package BD;
import model.DTO.Login;
import userDBProtocols.UsersDBProtocolInterface;
import utils.Log;
import model.DTO.CreateUserRequest;
import model.DTO.RESPONSE_CODE;
import model.DTO.Response;
import model.Users;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;

public class ProtocolUsersDBImpl extends UnicastRemoteObject implements UsersDBProtocolInterface, Serializable {
    private static final long serialVersionUID = 1L;
    private ConcurrentSkipListMap<String, Users> db;
    private Supplier<String> filePathSupplier;
    private Log userDBLogger;

    public ProtocolUsersDBImpl() throws RemoteException {
        // creating users.db file
        filePathSupplier = () -> {
            try {
                String userHome = System.getProperty("user.home");
                Path documentsPath = Paths.get(userHome, "Desktop", "carStore", "UserDB");

                if (!Files.exists(documentsPath)) {
                    Files.createDirectories(documentsPath);
                }

                return documentsPath.toString();
            } catch (Exception e) {
                throw new RuntimeException("Error creating path", e);
            }
        };

        userDBLogger = new Log(filePathSupplier.get(), "UsersDBLogger");
        userDBLogger.logInfo(() -> "log initialized");

        String filePath = filePathSupplier.get() + File.separator + "Users.db";
        File file = new File(filePath);

        if (file.exists()) {
            loadDatabase(filePath);
        } else {
            db = new ConcurrentSkipListMap<>();
            saveDbToFile();
            userDBLogger.logInfo(() -> "new db created, root path is-> " + filePath);
        }
    }

    private void loadDatabase(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            db = (ConcurrentSkipListMap<String, Users>) ois.readObject();
            userDBLogger.logInfo(() -> "db loaded from " + filePath);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading database " + filePath, e);
        }
    }

    private void saveDbToFile() {
        String filePath = filePathSupplier.get() + File.separator + "Users.db";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(db);
            userDBLogger.logInfo(() -> "database saved successfully at " + filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error saving database " + filePath, e);
        }
    }

    public void backupDb(String backupDirectory) {
        String filePath = filePathSupplier.get() + File.separator + "Users.db";

        if (backupDirectory == null || backupDirectory.isEmpty()) {
            backupDirectory = filePathSupplier.get();
        }

        String backupFilePath = backupDirectory + File.separator + "Users_Backup.db";

        try {
            Files.copy(Paths.get(filePath), Paths.get(backupFilePath));
            userDBLogger.logInfo(() -> "backup completed successfully. backup file: " + backupFilePath);
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
            CreateUserRequest userRequest = CreateUserRequest.fromString(request);
            Users newUser = new Users();
            synchronized (db) {
                if (db.containsKey(userRequest.login())) {
                    return new Response(RESPONSE_CODE.CONFLICT, RESPONSE_CODE.CONFLICT.getDescription()).toString();
                }

                newUser = new Users(UUID.randomUUID(), userRequest.login(), userRequest.password(), userRequest.policy());
                db.put(newUser.getLogin(), newUser);
                saveDbToFile();
            }

            Users finalNewUser = newUser;
            userDBLogger.logInfo(() -> "NEW USER CREATED WITH ID->" + finalNewUser.getId());
            return new Response(RESPONSE_CODE.CREATED, RESPONSE_CODE.CREATED.getDescription()).toString();

        } catch (IllegalArgumentException e) {
            userDBLogger.logWarning(() -> "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            userDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    // read by key
    public String read(String request) throws RemoteException {
        return db.containsKey(request)
                ? new Response(RESPONSE_CODE.FOUND, db.get(request).toString()).toString()
                : new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
    }

    @Override
    public String update(String request) throws RemoteException {
        try {
            CreateUserRequest userRequest = CreateUserRequest.fromString(request);

            synchronized (db) {
                Users existingUser = db.get(userRequest.login());

                if (existingUser != null) {
                    existingUser.setPassword(userRequest.password());
                    existingUser.setPolicy(userRequest.policy());
                    saveDbToFile();

                    userDBLogger.logInfo(() -> "USER UPDATED WITH ID->" + existingUser.getId());
                    return new Response(RESPONSE_CODE.OK, RESPONSE_CODE.OK.getDescription()).toString();
                } else {
                    return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
                }
            }
        } catch (IllegalArgumentException e) {
            userDBLogger.logWarning(() -> "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            userDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    public String delete(String request) throws RemoteException {
        try {
            synchronized (db) {
                if (db.containsKey(request)) {
                    db.remove(request);
                    saveDbToFile();

                    userDBLogger.logInfo(() -> "USER DELETED WITH LOGIN->" + request);
                    return new Response(RESPONSE_CODE.NO_CONTENT, RESPONSE_CODE.NO_CONTENT.getDescription()).toString();
                } else {
                    return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
                }
            }
        } catch (Exception e) {
            userDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }

    @Override
    public String searchByLogin(String request) throws RemoteException {
        return  db.entrySet().stream()
                .filter(entry -> entry.getKey().equals(request))
                .findFirst()
                .map(entry -> new Response(RESPONSE_CODE.FOUND, entry.getValue().toString()).toString())
                .orElse(new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString());
    }

    @Override
    public String authenticate(String request) throws RemoteException {
        try {
            Login userRequest = Login.fromString(request);

            synchronized (db) {
                Users existingUser = db.get(userRequest.username());

                if (existingUser != null) {
                    if (existingUser.getPassword().equals(userRequest.password())) {
                        userDBLogger.logInfo(() -> "USER AUTHENTICATED WITH LOGIN->" + userRequest.username());
                        return new Response(RESPONSE_CODE.OK, RESPONSE_CODE.OK.getDescription()).toString();
                    } else {
                        userDBLogger.logWarning(() -> "INVALID PASSWORD FOR LOGIN->" + userRequest.username());
                        return new Response(RESPONSE_CODE.UNAUTHORIZED, RESPONSE_CODE.UNAUTHORIZED.getDescription()).toString();
                    }
                } else {
                    return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
                }
            }
        } catch (IllegalArgumentException e) {
            userDBLogger.logWarning(() -> "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            userDBLogger.logError(e::getMessage);
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }
}