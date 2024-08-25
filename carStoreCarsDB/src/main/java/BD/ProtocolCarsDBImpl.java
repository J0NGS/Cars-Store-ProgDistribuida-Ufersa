package BD;

import carsDBProtocols.CarsDBProtocolInterface;
import model.Cars;
import model.DTO.*;
import utils.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class ProtocolCarsDBImpl extends UnicastRemoteObject implements CarsDBProtocolInterface, Serializable {
    private static final long serialVersionUID = 1L;
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, CopyOnWriteArrayList<Cars>>> db;
    private Supplier<String> filePathSupplier;
    private Log carsDBLogger;


    public ProtocolCarsDBImpl() throws RemoteException {
        filePathSupplier = () -> {
            try {
                String userHome = System.getProperty("user.home");
                Path documentsPath = Paths.get(userHome, "Desktop", "carStore", "CarsDB");

                if (!Files.exists(documentsPath)) {
                    Files.createDirectories(documentsPath);
                }

                return documentsPath.toString();
            } catch (Exception e) {
                throw new RuntimeException("Error creating path", e);
            }
        };

        carsDBLogger = new Log(filePathSupplier.get(), "CarsDBLogger");
        carsDBLogger.logInfo(() -> "log initialized");

        String filePath = filePathSupplier.get() + File.separator + "Cars.db";
        File file = new File(filePath);

        if (file.exists()) {
            loadDatabase(filePath);
        } else {
            db = new ConcurrentHashMap<>();
            saveDbToFile();
            carsDBLogger.logInfo(() -> "new db created, root path is-> " + filePath);
        }
    }

    private void loadDatabase(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            db = (ConcurrentHashMap<String, ConcurrentHashMap<Integer, CopyOnWriteArrayList<Cars>>>) ois.readObject();
            carsDBLogger.logInfo(() -> "db loaded from " + filePath);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading database " + filePath, e);
        }
    }

    private void saveDbToFile() {
        String filePath = filePathSupplier.get() + File.separator + "Cars.db";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(db);
            carsDBLogger.logInfo(() -> "database saved successfully at " + filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error saving database " + filePath, e);
        }
    }

    public void backupDb(String backupDirectory) {
        String filePath = filePathSupplier.get() + File.separator + "Cars.db";
        String backupFilePath = (backupDirectory == null || backupDirectory.isEmpty() ? filePathSupplier.get() : backupDirectory) + File.separator + "Cars_Backup.db";

        try {
            Files.copy(Paths.get(filePath), Paths.get(backupFilePath));
            carsDBLogger.logInfo(() -> "backup completed successfully. backup file: " + backupFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error performing backup of the database " + backupFilePath, e);
        }
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    @Override
    public String create(String request) throws RemoteException {
        try {
            CreateCarRequest carRequest = CreateCarRequest.fromString(request);
            Cars newCar = new Cars(UUID.randomUUID(), carRequest.name(), carRequest.category(), carRequest.year(), carRequest.price(), carRequest.renavam());

            boolean carExists = db.values().stream()
                    .flatMap(yearMap -> yearMap.values().stream())
                    .flatMap(CopyOnWriteArrayList::stream)
                    .anyMatch(car -> car.getRenavam().equals(newCar.getRenavam()));

            if (carExists) {
                carsDBLogger.logWarning(() -> "CAR WITH RENAVAM ALREADY EXISTS->" + newCar.getRenavam());
                return new Response(RESPONSE_CODE.CONFLICT, "Car with the same renavam already exists").toString();
            }

            db.computeIfAbsent(newCar.getName(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(newCar.getYearOfManufacture(), k -> new CopyOnWriteArrayList<>())
                    .add(newCar);

            saveDbToFile();
            carsDBLogger.logInfo(() -> "NEW CAR CREATED WITH ID->" + newCar.getId());
            return new Response(RESPONSE_CODE.CREATED, RESPONSE_CODE.CREATED.getDescription()).toString();

        } catch (IllegalArgumentException e) {
            carsDBLogger.logWarning(() -> "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            carsDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    public String read(String renavam) throws RemoteException {
        try {
            return db.values().stream()
                    .flatMap(yearMap -> yearMap.values().stream())
                    .flatMap(CopyOnWriteArrayList::stream)
                    .filter(car -> car.getRenavam().equals(renavam))
                    .findFirst()
                    .map(Cars::toString)
                    .orElse(new Response(RESPONSE_CODE.NOT_FOUND, "Car not found").toString());
        } catch (Exception e) {
            carsDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    public String getAll() throws RemoteException {
        try {
            StringBuilder result = new StringBuilder("All cars:\n");
            db.forEach((model, yearMap) -> {
                yearMap.forEach((year, carsList) -> {
                    carsList.forEach(car -> result.append(car).append("\n"));
                });
            });

            if (result.length() > 0) {
                return new Response(RESPONSE_CODE.OK, result.toString()).toString();
            } else {
                return new Response(RESPONSE_CODE.NOT_FOUND, "No cars found").toString();
            }
        } catch (Exception e) {
            carsDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }



    @Override
    public String delete(String renavam) throws RemoteException {
        try {
            boolean removed = db.values().stream()
                    .flatMap(yearMap -> yearMap.values().stream())
                    .flatMap(CopyOnWriteArrayList::stream)
                    .filter(car -> car.getRenavam().equals(renavam))
                    .findFirst()
                    .map(car -> {
                        db.get(car.getName()).get(car.getYearOfManufacture()).remove(car);
                        return true;
                    })
                    .orElse(false);

            saveDbToFile();
            return removed ?
                    new Response(RESPONSE_CODE.OK, RESPONSE_CODE.OK.getDescription()).toString() :
                    new Response(RESPONSE_CODE.NOT_FOUND, "Car not found").toString();
        } catch (Exception e) {
            carsDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    public String searchByRenavam(String request) throws RemoteException {
        try {
            String renavam = request.trim();

            return db.values().stream()
                    .flatMap(yearMap -> yearMap.values().stream())
                    .flatMap(CopyOnWriteArrayList::stream)
                    .filter(car -> car.getRenavam().equals(renavam))
                    .findFirst()
                    .map(car -> new Response(RESPONSE_CODE.OK, "Car found: " + car).toString())
                    .orElse(new Response(RESPONSE_CODE.NOT_FOUND, "Car not found").toString());
        } catch (Exception e) {
            carsDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    public String searchByModel(String request) throws RemoteException {
        try {
            String model = request.trim().toLowerCase();

            String result = db.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains(model))
                    .flatMap(entry -> entry.getValue().values().stream())
                    .flatMap(CopyOnWriteArrayList::stream)
                    .map(Cars::toString)
                    .reduce("Cars found:\n", (acc, car) -> acc + car + "\n");

            return result.isEmpty() ?
                    new Response(RESPONSE_CODE.NOT_FOUND, "No cars found for the model").toString() :
                    new Response(RESPONSE_CODE.OK, result).toString();
        } catch (Exception e) {
            carsDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    public String update(String request) throws RemoteException {
        try {
            CreateCarRequest carRequest = CreateCarRequest.fromString(request);
            Cars updatedCar = new Cars(UUID.randomUUID(), carRequest.name(), carRequest.category(), carRequest.year(), carRequest.price(), carRequest.renavam());

            delete(updatedCar.getRenavam());

            db.computeIfAbsent(updatedCar.getName(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(updatedCar.getYearOfManufacture(), k -> new CopyOnWriteArrayList<>())
                    .add(updatedCar);

            saveDbToFile();
            return new Response(RESPONSE_CODE.OK, RESPONSE_CODE.OK.getDescription()).toString();
        } catch (IllegalArgumentException e) {
            carsDBLogger.logWarning(() -> "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            carsDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }
}
