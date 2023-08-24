package com.viktorskala;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MathEquation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String equation;
    private double root;

    public MathEquation() {
    }

    public Long getId() {
        return id;
    }

    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public double getRoot() {
        return root;
    }

    public void setRoot(double root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return "MathEquations{" +
                "id=" + id +
                ", equation='" + equation + '\'' +
                ", root=" + root +
                '}';
    }
}
