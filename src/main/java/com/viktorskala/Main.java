package com.viktorskala;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Scanner;

public class Main {

    public static EntityManagerFactory emf;
    public static EntityManager em;

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        try {
            EquationService equationService = new EquationService();
            emf = Persistence.createEntityManagerFactory("Equation");
            em = emf.createEntityManager();
            try {
                while (true) {
                    System.out.println("1: enter equation");
                    System.out.println("2: enter root of equation");
                    System.out.println("3: get all equations with one of the next roots");
                    System.out.println("4: get all equations with this root");
                    System.out.print("-> ");

                    String s = scanner.nextLine();
                    switch (s) {
                        case "1":
                            System.out.println("case - 1");
                            equationService.addEquation(scanner);
                            break;
                        case "2":
                            System.out.println("case - 2");
                            equationService.addRoot(scanner);
                            break;
                        case "3":
                            System.out.println("case - 3");
                            equationService.getEquationsForSeveralRoots(scanner);
                            break;
                        case "4":
                            System.out.println("case - 4");
                            equationService.getEquationsForOneRoot(scanner);
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                scanner.close();
                em.close();
                emf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
