import Exceptions.AlreadyInTable;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Admin {
    private Registry registry;
    public DataProvider db;

    public Admin(Registry r, DataProvider dp){
        this.registry = r;
        this.db = dp;
    }

    public static void main(String[] args) {
        Registry registo = null;
        try {
            registo = LocateRegistry.getRegistry(args[0]);
            registo.list();
        } catch (RemoteException e) {
            System.err.println("Could not retrieve the RMI registry! Make sure it is running");
            return;
        }
        System.out.println("RMI registry obtained!");

        DataProvider dataProvider = null;
        try{
            dataProvider = (DataProvider) registo.lookup("database");
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;
        } catch (NotBoundException e) {
            e.printStackTrace();
            return;
        }

        Admin admin = new Admin(registo, dataProvider);
        while (true){
            int option = admin.menu();
            switch (option) {
                case 1:
                    admin.menuFac();
                    break;
                case 2:
                    admin.menuPessoa();
                    break;
                case 3:
                    admin.menuMesas();
                    break;
                case 4:
                    admin.menuPessoa();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida, tenta novamente.");
                    break;
            }
        }
    }

    private int menu() {
        Scanner sc = new Scanner(System.in);
        int option = 0;
        System.out.println("");
        System.out.println("");
        System.out.println("  ******************************************************************************");
        System.out.println("  *                            Admin Console - iVotas                          *");
        System.out.println("  *                                                                            *");
        System.out.println("  *     1-Gerir departamentos e faculdades                                     *");
        System.out.println("  *     2- Gerir Pessoas                                                        *");
        System.out.println("  *     3-Gerir mesas de voto                             *");
        System.out.println("  *     4-Gerir eleicoes                                                  *");
        System.out.println("  *     0 - Exit                                                              *");
        System.out.println("  *                                                                            *");
        System.out.println("  ******************************************************************************");
        System.out.print(" Option: ");
        try {
            option = sc.nextInt();
        } catch (Exception e) {
            option = 0;
        }
        return option;
    }

    private void menuFac(){
        Scanner sc = new Scanner(System.in);
        while (true) {
            int option = 0;
            System.out.println("  ******************************************************************************");
            System.out.println("  *     1- Criar Faculdade                                     *");
            System.out.println("  *     2- Listar Faculdades                                                        *");
            System.out.println("  *     3- Eliminar Faculdades                             *");
            System.out.println("  *     4- Criar Departamento                                                  *");
            System.out.println("  *     5- Listar Departamento                                                  *");
            System.out.println("  *     6- Eliminar Departamento                                                  *");
            System.out.println("  *     0 - Go back                                                              *");
            try {
                option = sc.nextInt();
            } catch (Exception e) {
                option = 0;
            }
            sc.nextLine();
            switch (option){
                case 1:
                    System.out.println("Insira o nome da faculdade:");
                    String nome = sc.nextLine();
                    if (nome.equals("")){
                        System.out.println("Criacao cancelada");
                        break;
                    }
                    try {
                        this.db.createFaculdade(nome);
                    } catch (SQLException e) {
                        if (e.getMessage().contains("UPPER_NOME")){
                            System.out.println("Name must be all capitals");
                            this.rebuildStatements();
                        }
                        else {
                            e.printStackTrace();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    this.imprimirFaculdades();
                    break;
                case 3:
                    this.imprimirFaculdades();
                    System.out.println("Selecione o id da faculdade a eliminar:");
                    int id = sc.nextInt();
                    System.out.println(id);
                    try {
                        this.db.deleteFaculdade(id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    System.out.println("Insira o nome do departamento:");
                    nome = sc.nextLine();
                    if (nome.equals("")){
                        System.out.println("Criacao cancelada");
                        break;
                    }
                    this.imprimirFaculdades();
                    System.out.println("Selecione o id da faculdade a que o departamento pertence. ( 0 = independente)");
                    int fac_id = sc.nextInt();
                    try {
                        if (fac_id == 0){
                            this.db.createDepartamento(nome);
                        }
                        else {
                            this.db.createDepartamento(nome, fac_id);
                        }
                    } catch (SQLException e) {
                        if (e.getMessage().contains("UPPER_NOME")){
                            System.out.println("Name must be all capitals");
                            this.rebuildStatements();
                        }
                        else if(e.getMessage().contains("FOREIGN KEY")){
                            System.out.println("Id faculdade invalido");
                            this.rebuildStatements();
                        }
                        else {
                            e.printStackTrace();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    imprimirDepartamentos();
                    break;
                case 6:
                    this.imprimirDepartamentos();
                    System.out.println("Selecione o id do departamento a eliminar:");
                    id = sc.nextInt();
                    System.out.println(id);
                    try {
                        this.db.deleteDepartamento(id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida, tenta novamente.");
                    break;
            }
        }
    }


    private void menuPessoa(){
        Scanner sc = new Scanner(System.in);
        while (true) {
            int option = 0;
            System.out.println("  ******************************************************************************");
            System.out.println("  *     1- Criar Pessoa                                     *");
            System.out.println("  *     2- Listar Pessoas                                                        *");
            System.out.println("  *     3- Eliminar Pessoa                             *");
            System.out.println("  *     4- Alterar morada                             *");
            System.out.println("  *     5- Alterar telefone                             *");
            System.out.println("  *     6- Alterar credenciais                             *");
            System.out.println("  *     7- Alterar nome                             *");
            System.out.println("  *     8- Alterar cc                             *");
            System.out.println("  *     0 - Go back                                                              *");
            try {
                option = sc.nextInt();
            } catch (Exception e) {
                option = 0;
            }
            sc.nextLine();
            switch (option){
                case 1:
                    this.criarPessoa();
                    break;
                case 2:
                    this.imprimirPessoas();
                    break;
                case 3:
                    this.imprimirPessoas();
                    System.out.println("Selecione o id da pessoa a eliminar:");
                    int id = sc.nextInt();
                    try {
                        this.db.deletePessoa(id);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    int choice = selectPessoa();
                    System.out.println("Insira a morada:");
                    String morada = sc.nextLine();
                    try {
                        this.db.changeAddress(choice, morada);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    choice = selectPessoa();
                    System.out.println("Insira o telefone:");
                    Integer phone = sc.nextInt();
                    try {
                        this.db.changePhone(choice, phone);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 6:
                    choice = selectPessoa();
                    System.out.println("Insira o username:");
                    String username = sc.nextLine();
                    System.out.println("Insira o password:");
                    String password = sc.nextLine();
                    try {
                        this.db.changeCredentials(choice, username, password);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 7:
                    choice = selectPessoa();
                    System.out.println("Insira o nome:");
                    String nome = sc.nextLine();
                    try {
                        this.db.changeName(choice, nome);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 8:
                    choice = selectPessoa();
                    System.out.println("Insira o cc:");
                    String cc = sc.nextLine();
                    System.out.println("Insira a validade:");
                    String validade = sc.nextLine();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                    Date valcc = dateFormat.parse(validade);
                        this.db.changeCC(choice, cc, valcc);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida, tenta novamente.");
                    break;
            }
        }
    }

    private void menuEleicao() throws SQLException, RemoteException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            int option = 0;
            System.out.println("  ******************************************************************************");
            System.out.println("  *     1- Criar Eleicao                                     *");
            System.out.println("  *     2- Listar Eleicao                                                        *");
            System.out.println("  *     3- Eliminar Eleicao                             *");
            System.out.println("  *     0 - Go back                                                              *");
            try {
                option = sc.nextInt();
            } catch (Exception e) {
                option = 0;
            }
            sc.nextLine();
            switch (option){
                case 1:
                    this.criarEleicao();
                    break;
                case 2:
                    this.imprimirEleicoes();
                    break;
                case 3:
                    this.imprimirEleicoes();
                    System.out.println();
                    int choice = sc.nextInt();
                    this.db.deleteEleicao(choice);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida, tenta novamente.");
                    break;
            }
        }
    }

    private void menuMesas(){
        Scanner sc = new Scanner(System.in);
        while (true) {
            int option = 0;
            System.out.println("  ******************************************************************************");
            System.out.println("  *     1- Criar Mesa                                     *");
            System.out.println("  *     2- Listar Mesas                                                        *");
            System.out.println("  *     3- Eliminar Mesa                             *");
            System.out.println("  *     0 - Go back                                                              *");
            try {
                option = sc.nextInt();
            } catch (Exception e) {
                option = 0;
            }
            sc.nextLine();
            switch (option){
                case 1:
                    this.imprimirDepartamentos();
                    int dep_id = sc.nextInt();
                    int member1 = this.selectPessoa();
                    int member2 = this.selectPessoa();
                    int member3 = this.selectPessoa();
                    try {
                        this.db.createMesa(dep_id, member1, member2, member3);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (AlreadyInTable alreadyInTable) {
                        if (alreadyInTable.getMessage().equals("Department")){
                            System.out.println("Department already has a table");
                        }
                        else {
                            System.out.println("A table cannot have the same person more than once");
                        }
                        rebuildStatements();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    this.imprimirMesas();
                    break;
                case 3:
                    this.imprimirMesas();
                    System.out.println("Escolha o id da mesa a eliminar");
                    int choice = sc.nextInt();
                    try {
                        this.db.deleteMesa(choice);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opção inválida, tenta novamente.");
                    break;
            }
        }
    }

    private int selectPessoa(){
        Scanner sc = new Scanner(System.in);
        this.imprimirPessoas();
        System.out.println("Selecione o id da pessoa:");
        int choice = sc.nextInt();
        return choice;
    }

    private void imprimirFaculdades(){
        try {
            String[][] sts = this.db.getAllFaculdades();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void imprimirDepartamentos(){
        try {
            String[][] sts = this.db.getAllDepartamentos();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void rebuildStatements(){
        System.out.println("Rebuilding SQL statements");
        try {
            this.db.prepareStatements();
        } catch (SQLException e1) {
            e1.printStackTrace();
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    private void imprimirPessoas(){
        try {
            String[][] sts = this.db.getAllPessoas();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void criarPessoa(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Insira o nome:");
        String nome = sc.nextLine();
        if (nome.equals("")){
            System.out.println("Criacao cancelada");
            return;
        }
        System.out.println("Insira o cc:");
        String cc = sc.nextLine();
        if (cc.equals("")){
            System.out.println("Criacao cancelada");
            return;
        }

        System.out.println("Insira o username:");
        String username = sc.nextLine();
        if (username.equals("")){
            System.out.println("Criacao cancelada");
            return;
        }

        System.out.println("Insira a password:");
        String password = sc.nextLine();
        if (password.equals("")){
            System.out.println("Criacao cancelada");
            return;
        }
        System.out.println("Faculdade(0) ou departamento(>0 || <0)");
        Integer dep_id = null;
        Integer fac_id = null;
        int choice = sc.nextInt();
        if(choice == 0){
            this.imprimirFaculdades();
            System.out.println("Escolha o id");
            fac_id = sc.nextInt();
        }
        else {
            this.imprimirDepartamentos();
            System.out.println("Escolha o id");
            dep_id = sc.nextInt();
        }
        sc.nextLine();
        System.out.println("0 (default) - " + Database.PersonType.STUDENT.toString());
        System.out.println("1 - " + Database.PersonType.TEACHER.toString());
        System.out.println("2 - " + Database.PersonType.JANITOR.toString());
        Database.PersonType funcao = Database.PersonType.STUDENT;
        choice = sc.nextInt();
        if (choice == 1){
            funcao = Database.PersonType.TEACHER;
        }
        else if(choice == 2){
            funcao = Database.PersonType.JANITOR;
        }

        try {
            this.db.createPessoaFullData(nome, cc, null, username, password,
                    null, null, funcao, dep_id, fac_id);
        } catch (SQLException e) {
            if(e.getMessage().contains("FOREIGN KEY")){
                System.out.println("Id faculdade/departamento invalido");
                this.rebuildStatements();
            }
            else {
                e.printStackTrace();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void imprimirMesas(){
        try {
            String[][] sts = this.db.getAllMesas();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void imprimirEleicoes(){
        try {
            String[][] sts = this.db.getAllElections();
            for (int i = 0; i < sts[0].length; i++) {
                for (int j = 0; j < sts.length; j++) {
                    System.out.print( sts[j][i] + " \t ");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void criarEleicao(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Scanner sc = new Scanner(System.in);
        System.out.println("Insira a data de inicio");
        String inicio_st = sc.nextLine();
        System.out.println("Insira a data de fim");
        String fim_st = sc.nextLine();

        Date inicio = null;
        Date fim = null;
        try {
            inicio = dateFormat.parse(inicio_st);
            fim = dateFormat.parse(fim_st);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        if (inicio.compareTo(fim) > 0){
            System.out.println("O inicio nao pode ser depois do fim");
            return;
        }

        System.out.println("Insira o titulo:");
        String titulo = sc.nextLine();
        System.out.println("Insira a descricao:");
        String descricao = sc.nextLine();

        int choice = 0;
        Database.ElectionType tipo = Database.ElectionType.CONSELHO_GERAL;
        System.out.println("0 (default) - " + Database.ElectionType.CONSELHO_GERAL.toString());
        System.out.println("1 - " + Database.ElectionType.NUCLEO_ESTUDANTES.toString());
        System.out.println("2 - " + Database.ElectionType.DEPARTAMENTO.toString());
        System.out.println("3 - " + Database.ElectionType.FACULDADE.toString());
        choice = sc.nextInt();
        if (choice == 1){
            tipo = Database.ElectionType.NUCLEO_ESTUDANTES;
        }
        else if(choice == 2){
            tipo = Database.ElectionType.DEPARTAMENTO;
        }
        else if (choice == 3){
            tipo = Database.ElectionType.FACULDADE;
        }

        Integer dep_id = null;
        Integer fac_id = null;
        sc.nextLine();
        if (tipo == Database.ElectionType.FACULDADE){
            this.imprimirFaculdades();
            choice = sc.nextInt();
            fac_id = choice;
        }
        else if(tipo != Database.ElectionType.CONSELHO_GERAL){
            this.imprimirDepartamentos();
            choice = sc.nextInt();
            dep_id = choice;
        }

        try {
            this.db.createEleicaoFull(inicio, fim, titulo, descricao, dep_id, fac_id, tipo);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
