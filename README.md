# Projeto de Programação Distribuída

Este repositório contém o código fonte e a documentação do projeto 1 de Programação Distribuída da disciplina de Projetos da Universidade Federal Rural do Semi-Árido (UFERSA).

## Estrutura do Projeto
![image](https://github.com/user-attachments/assets/a2b5c1c0-d578-4c0f-833c-e018afb678fd)
# carStore
    Aplicação cliente que consome o gateway
# carStoreCarsDB
    Aplicação que simula o banco de dados da entidade Cars
# carStoreCarsDBDiscoveryServer
    Aplicação que fornece uma instância de CarsDB disponivel para operação
# carStoreCarsService
    Aplicação Service da entidade Cars
# carStoreCarsServiceDiscoveryServer
    Aplicação que fornece uma instância de CarsService disponivel para operação
# carStoreGateway
    Aplicação centralizadora que comunica com o discoveryServers dos Services
# carStoreProtocols
    Pacote de utilitários, modelagem, e padronização da comunicação da aplicação, está sendo utilizado em todas as outras aplicações
# carStoreUserDB
    Aplicação que simula o banco de dados da entidade User
# carStoreUsersDBDiscoveryServer
    Aplicação que fornece uma instância de UsersDB disponivel para operação
# carStoreUsersService
    Aplicação Service da entidade Users
# carStoreUsersServiceDiscoveryServer
    Aplicação que fornece uma instância de UsersService disponivel para operação

## Como Executar

Se você deseja executar o projeto pode seguir os passos abaixo:
# OBS:INSTÂNCIAS DO DB EXECUTAVEIS APENAS NO WINDOWS(CRIE UMA PASTA CHAMADA carStore NO DESKTOP)
1. Execute todos os dicoveryServers e sete as suas portas de execução.
    - Recomendação de portas 
        - dicosceryServer CARS DB(discoveryServerCarsDB.jar):8080
        - dicoveryServer CARS SERVICE(discoveryServerCarsService.jar):8282
        - dicosceryServer USERS DB(discoveryServerUsersDB.jar):7070	
        - dicoveryServer USERS SERVICE(discoveryServerUsersService.jar):7272
2. Execute os DBs(SOMENTE WINDOWS) e os Services.
    ATENÇÃO: Preste muita atenção nos endereços que cada aplicação vai pedir, constatemente vão ser pedido endereços dos discoverys Servers.
    - Recomendação de portas 
        - CARS DB(carsDbServer.jar):8181 - 8199
        - CARS SERVICE(carsServiceServer.jar):8383 - 8399
        - USERS DB(usersDbServer.jar):7171 - 7199	
        - USERS SERVICE(usersService.jar):7373 - 7399
3. Execute o gateway(carStoreGateway.jar).
    - Forneça ao gateway os endereços dos discoverys servers dos services
4. Execute a aplicação cliente(carStore.jar).
    - Forneça o endereço do gateawy
5. O gateway por padrão cadastra um usuário : login=admin, senha=admin. Caso ele já exista será exibida uma warning de conflict no gateway, não influência em nada.
6. O DB de Cars por padrão cadastra 30 carros setados em código, caso não exista nenhum DB.


## How to Run

If you want to run the project, you can follow the steps below:
# NOTE: EXECUTABLE DB INSTANCES ONLY ON WINDOWS (CREATE A FOLDER CALLED carStore ON DESKTOP)
1. Start all the discovery servers and set their execution ports.
    - Recommended ports:
        - Cars DB Discovery Server (discoveryServerCarsDB.jar): 8080
        - Cars Service Discovery Server (discoveryServerCarsService.jar): 8282
        - Users DB Discovery Server (discoveryServerUsersDB.jar): 7070
        - Users Service Discovery Server (discoveryServerUsersService.jar): 7272
2. Start the DBs (WINDOWS ONLY) and the Services.
    ATTENTION: Pay close attention to the addresses that each application will ask for, as they will constantly ask for discovery server addresses.
    - Recommended ports:
        - Cars DB (carsDbServer.jar): 8181 - 8199
        - Cars Service (carsServiceServer.jar): 8383 - 8399
        - Users DB (usersDbServer.jar): 7171 - 7199
        - Users Service (usersService.jar): 7373 - 7399
3. Start the gateway (carStoreGateway.jar).
    - Provide the gateway with the addresses of the service discovery servers.
4. Start the client application (carStore.jar).
    - Provide the gateway address.
5. The gateway by default registers a user: login=admin, password=admin. If the user already exists, a conflict warning will be displayed in the gateway, but it does not affect anything.
6. The Cars DB by default registers 30 cars set in code, if no DB exists.
