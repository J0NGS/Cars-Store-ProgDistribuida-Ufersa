package BD;

import UserBDProtocols.UserBDProtocolInterface;
import Utils.Log;
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
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class ProtocolUsersBDImpl extends UnicastRemoteObject implements UserBDProtocolInterface, Serializable {
    private static final long serialVersionUID = 1L;
    private ConcurrentSkipListMap<String, Users> db;
    private Supplier<String> FILE_PATH;
    private Log userDBLogger;

    public ProtocolUsersBDImpl() throws RemoteException {
        // creating users.db file
        FILE_PATH = () -> {
            try {
                // path to the user's "documents" folder
                String userHome = System.getProperty("user.home");
                Path documentsPath = Paths.get(userHome, "Desktop", "carStore", "UserDB");

                // checks if the folder exists, otherwise, creates the folder
                if (!Files.exists(documentsPath)) {
                    Files.createDirectories(documentsPath);
                }

                // returns the path
                return documentsPath.toString();
            } catch (Exception e) {
                throw new RuntimeException("Error creating path", e);
            }
        };
        userDBLogger = new Log(FILE_PATH.get(), "UsersDBLogger");
        userDBLogger.write("INFO", "log initialized");
        String filePath = FILE_PATH.get() + File.separator + "Users.db";
        File file = new File(filePath);

        if (file.exists()) {
            // if the file exists, load the ConcurrentSkipListMap
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                db = (ConcurrentSkipListMap<String, Users>) ois.readObject();
                System.out.println("database loaded successfully " + filePath);
                userDBLogger.write("INFO", "db loaded");
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("Error loading database " + filePath, e);
            }
        } else {
            // if the file does not exist, initialize a new ConcurrentSkipListMap
            db = new ConcurrentSkipListMap<>();
            saveDbToFile();
            System.out.println("new db created, root path is-> " + filePath);
            userDBLogger.write("INFO", "new db created");
        }
    }

    // save the ConcurrentSkipListMap to the file
    private void saveDbToFile() {
        String filePath = FILE_PATH.get() + File.separator + "Users.db";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(db);
            System.out.println("database saved successfully at " + filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error saving database " + filePath, e);
        }
    }

    // perform database backup
    public void backupDb(String backupDirectory) {
        String filePath = FILE_PATH.get() + File.separator + "Users.db";

        // if the backupDirectory is not provided, use the default path
        if (backupDirectory == null || backupDirectory.isEmpty()) {
            backupDirectory = FILE_PATH.get();
        }

        String backupFilePath = backupDirectory + File.separator + "Users_Backup.db";

        try {
            // copy the users.db file to the specified (or default) location with the _Backup suffix
            Files.copy(Paths.get(filePath), Paths.get(backupFilePath));
            System.out.println("backup completed successfully. backup file: " + backupFilePath);
            userDBLogger.write("INFO", "backup completed successfully.");
        } catch (IOException e) {
            throw new RuntimeException("error performing backup of the database " + backupFilePath, e);
        }
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    @Override
    public String create(String request) throws RemoteException {
        try {
            // parsing da requisição
            CreateUserRequest userRequest = CreateUserRequest.fromString(request);

            // criando um novo user
            synchronized (db){
                if (db.containsKey(userRequest.login()))
                    return new Response(RESPONSE_CODE.CONFLICT, RESPONSE_CODE.CONFLICT.getDescription()).toString();
            }
            Users newUser = new Users();
            newUser.setId(UUID.randomUUID());
            newUser.setLogin(userRequest.login());
            newUser.setPassword(userRequest.password());
            newUser.setPolicy(userRequest.policy());

            // sync o acesso ao banco de dados
            synchronized (db) {
                db.put(newUser.getLogin(), newUser);
                saveDbToFile();
            }

            userDBLogger.write("CREATED", "NEW USER CREATED WITH ID->" + newUser.getId().toString());
            return new Response(RESPONSE_CODE.CREATED, RESPONSE_CODE.CREATED.getDescription()).toString();

        } catch (IllegalArgumentException e) {
            userDBLogger.write("BAD REQUEST", "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            e.printStackTrace();
            userDBLogger.write("ERROR",e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }


    @Override
    public String read(String request) throws RemoteException {
        synchronized (db){
            if (db.containsKey(request))
                return new Response(RESPONSE_CODE.FOUND,db.get(request).toString()).toString();
            else
                return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
        }
    }

    @Override
    public String update(String request) throws RemoteException {
        try {
            // Parsing da requisição para criar um objeto CreateUserRequest
            CreateUserRequest userRequest = CreateUserRequest.fromString(request);

            synchronized (db) {
                // Verificando se o usuário existe
                if (db.containsKey(userRequest.login())) {
                    Users existingUser = db.get(userRequest.login());

                    // Atualizando as informações do usuário
                    existingUser.setPassword(userRequest.password());
                    existingUser.setPolicy(userRequest.policy());

                    // Salvando as alterações no banco de dados
                    saveDbToFile();

                    userDBLogger.write("UPDATED", "USER UPDATED WITH ID->" + existingUser.getId().toString());
                    return new Response(RESPONSE_CODE.OK, RESPONSE_CODE.OK.getDescription()).toString();
                } else {
                    return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
                }
            }
        } catch (IllegalArgumentException e) {
            userDBLogger.write("BAD REQUEST", "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            e.printStackTrace();
            userDBLogger.write("ERROR", e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }


    @Override
    public String delete(String request) throws RemoteException {
        try {
            synchronized (db) {
                // Verificando se o usuário existe
                if (db.containsKey(request)) {
                    db.remove(request);
                    saveDbToFile();

                    userDBLogger.write("DELETED", "USER DELETED WITH LOGIN->" + request);
                    return new Response(RESPONSE_CODE.NO_CONTENT, RESPONSE_CODE.NO_CONTENT.getDescription()).toString();
                } else {
                    return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            userDBLogger.write("ERROR", e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }


    @Override
    public String searchByLogin(String request) throws RemoteException {
        synchronized (db) {
            if (db.containsKey(request)) {
                return new Response(RESPONSE_CODE.FOUND, db.get(request).toString()).toString();
            } else {
                return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
            }
        }
    }


    @Override
    public String authenticate(String request) throws RemoteException {
        try {
            // Parsing da requisição para criar um objeto CreateUserRequest
            CreateUserRequest userRequest = CreateUserRequest.fromString(request);

            synchronized (db) {
                // Verificando se o usuário existe e se a senha está correta
                if (db.containsKey(userRequest.login())) {
                    Users existingUser = db.get(userRequest.login());
                    if (existingUser.getPassword().equals(userRequest.password())) {
                        userDBLogger.write("AUTHENTICATED", "USER AUTHENTICATED WITH LOGIN->" + userRequest.login());
                        return new Response(RESPONSE_CODE.OK, RESPONSE_CODE.OK.getDescription()).toString();
                    } else {
                        userDBLogger.write("UNAUTHORIZED", "INVALID PASSWORD FOR LOGIN->" + userRequest.login());
                        return new Response(RESPONSE_CODE.UNAUTHORIZED, RESPONSE_CODE.UNAUTHORIZED.getDescription()).toString();
                    }
                } else {
                    return new Response(RESPONSE_CODE.NOT_FOUND, RESPONSE_CODE.NOT_FOUND.getDescription()).toString();
                }
            }
        } catch (IllegalArgumentException e) {
            userDBLogger.write("BAD REQUEST", "BAD REQUEST->" + e.getMessage());
            return new Response(RESPONSE_CODE.BAD_REQUEST, RESPONSE_CODE.BAD_REQUEST.getDescription()).toString();
        } catch (Exception e) {
            e.printStackTrace();
            userDBLogger.write("ERROR", e.getMessage());
            return new Response(RESPONSE_CODE.INTERNAL_SERVER_ERROR, RESPONSE_CODE.INTERNAL_SERVER_ERROR.getDescription()).toString();
        }
    }


}
