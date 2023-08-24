package com.viktorskala;

import javax.persistence.TypedQuery;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.viktorskala.Main.em;

public class EquationService {

    private boolean checkEquationForCorrect(String equation) {
        if (equation.equals("") & equation.equals(null)) {
            return false;
        }
        String o = "(-?\\(*)*";
        String c = "\\)*";
        String rightRegex1 = "((" + o + "\\d+(\\.?\\d+)*" + c + "[\\/*\\-+]" + o + ")*" + o + "x" + c + "(([\\/*\\-+]" + o + "\\d+(\\.?\\d+)*" + c + ")*([\\/*\\-+]" + o + "x" + c + ")*)*=-?\\d+(\\.?\\d+)*)";
        String rightRegex2 = "(-?\\d+(\\.?\\d+)*=(" + o + "\\d+(\\.?\\d+)*" + c + "[\\/*\\-+]" + o + ")*" + o + "x" + c + "(([\\/*\\-+]" + o + "\\d+(\\.?\\d+)*" + c + ")*([\\/*\\-+]" + o + "x" + c + ")*)*)";
        String rightRegex = rightRegex1 + "|" + rightRegex2;
        Pattern rightPattern = Pattern.compile(rightRegex);
        Matcher rightMatcher = rightPattern.matcher(equation);
        String wrongRegex = "(.*\\/+\\*+.*)|(.*\\*+/+.*)|(.*\\/+\\++.*)|(.*\\++/+.*)|(.*\\*+\\++.*)|(.*\\++\\*+.*)|(.*\\-+/+.*)|(.*\\-+\\*+.*)|(.*\\-+\\++.*)|(.*\\/{2,}.*)|(.*\\*{2,}.*)|(.*\\+{2,}.*)|(.*-{3,}.*)";
        Pattern wrongPattern = Pattern.compile(wrongRegex);
        Matcher wrongMatcher = wrongPattern.matcher(equation);
        AtomicInteger openBracket = new AtomicInteger(0);
        AtomicInteger closeBracket = new AtomicInteger(0);
        equation.chars().mapToObj(symbol -> (char) symbol)
                .forEach(symbol -> {
                    if (symbol == '(') {
                        openBracket.getAndIncrement();
                    }
                    if (symbol == ')') {
                        closeBracket.getAndIncrement();
                    }
                });
        if (rightMatcher.matches() & !wrongMatcher.matches() & (openBracket.get() == closeBracket.get())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkRootOfEquation(String equation, double root) throws ScriptException {
        String[] equationParts = equation.split("=");
        String partWithRoot = "";
        String partWithoutRoot = "";
        if (equationParts[0].contains("x")) {
            partWithRoot = equationParts[0];
            partWithoutRoot = equationParts[1];
        } else {
            partWithRoot = equationParts[1];
            partWithoutRoot = equationParts[0];
        }
        partWithRoot = partWithRoot.replace("x", String.valueOf(root));
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        double result = Double.valueOf(engine.eval(partWithRoot).toString());
        if (Math.abs(result - Double.valueOf(partWithoutRoot)) < Math.pow(10, -9)) {
            return true;
        } else {
            return false;
        }
    }

    public void addEquation(Scanner scanner) {
        System.out.println("Enter equation without whitespaces:");
        String equation = scanner.nextLine();
        if (checkEquationForCorrect(equation)) {
            TypedQuery query = em.createQuery("SELECT m FROM MathEquation m WHERE m.equation = '" + equation + "'", MathEquation.class);
            MathEquation mathEquation = null;
            try {
                mathEquation = (MathEquation) query.getSingleResult();
                System.out.println("such equation already exist.");
                return;
            } catch (Exception e) {
                mathEquation = new MathEquation();
            }
            mathEquation.setEquation(equation);
            em.getTransaction().begin();
            try {
                em.persist(mathEquation);
                em.getTransaction().commit();
                System.out.println(equation + " was successfully added.");
            } catch (Exception e) {
                System.out.println("problem");
                em.getTransaction().rollback();
                e.printStackTrace();
            }
        } else {
            System.out.println("Equation is not correct");
        }
    }


    public void addRoot(Scanner scanner) throws ScriptException {
        System.out.println("Please enter equation:");
        String equation = scanner.nextLine();
        if (!checkEquationForCorrect(equation)) {
            System.out.println("you entered equation incorrectly.");
            return;
        }
        System.out.println("Please enter root of the equation:");
        String line = scanner.nextLine();
        double root = 0;
        try {
            root = Double.valueOf(line);
        } catch (Exception e) {
            System.out.println("you entered the root of the equation incorrectly.");
            return;
        }
        if (!checkRootOfEquation(equation, root)) {
            System.out.println(root + " is not root of equation: " + equation);
            return;
        }
        TypedQuery query = em.createQuery("SELECT m FROM MathEquation m WHERE m.equation = '" + equation + "'", MathEquation.class);
        MathEquation mathEquation = null;
        Object mathEquationFromDB = null;
        try {
            mathEquationFromDB = query.getSingleResult();
            mathEquation = (MathEquation) mathEquationFromDB;
            mathEquation.setRoot(root);
            em.getTransaction().begin();
            try {
                em.merge(mathEquation);
                em.getTransaction().commit();
            } catch (Exception ex) {
                System.out.println("problem");
                em.getTransaction().rollback();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            mathEquation = new MathEquation();
            mathEquation.setEquation(equation);
            mathEquation.setRoot(root);
            em.getTransaction().begin();
            try {
                em.persist(mathEquation);
                em.getTransaction().commit();
            } catch (Exception ex) {
                System.out.println("problem");
                em.getTransaction().rollback();
                ex.printStackTrace();
            }
        }
    }

    public void getEquationsForSeveralRoots(Scanner scanner) {
        System.out.println("enter root without whitespaces, using \';\' (for example 1;2;3,5;");
        String line = scanner.nextLine();
        String[] numbers = line.split(";");
        for (String number: numbers) {
            double root = Double.valueOf(number);
            TypedQuery query = em.createQuery("SELECT m FROM MathEquation m WHERE m.root = '" + root + "'", MathEquation.class);
            List<Object> mathEquationsFromDB = query.getResultList();
            if (mathEquationsFromDB.size() > 0) {
                mathEquationsFromDB.stream().forEach(eq -> System.out.println(((MathEquation)eq).getEquation()));
            } else {
                System.out.println("There is not equations with such roots.");
            }
        }
    }

    public void getEquationsForOneRoot(Scanner scanner) {
        System.out.println("enter root:");
        String line = scanner.nextLine();
        double root = Double.valueOf(line);
        TypedQuery query = em.createQuery("SELECT m FROM MathEquation m WHERE m.root = '" + root + "'", MathEquation.class);
        List<Object> mathEquationsFromDB = query.getResultList();
        if (mathEquationsFromDB.size() > 0) {
            mathEquationsFromDB.stream().forEach(eq -> System.out.println(((MathEquation)eq).getEquation()));
        } else {
            System.out.println("There is not equations with such roots.");
        }
    }
}
