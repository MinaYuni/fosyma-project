package eu.su.mas.dedaleEtu.mas.knowledge;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;

import java.io.*;
import java.util.*;

public class HashMapSerialize {

    public HashMapSerialize(){
        super();
    }

    public static HashMap HashMapFrom(String s){
        // suppose que s est comme ça : {AgentFSM_1=[<Gold, 100>, <Diamond, 100>], AgentFSM_2=[]}
        HashMap<String, List<Couple<Observation,Integer>>> base = new HashMap(); //result

        String[] list = s.split("=");
        String key = list[0].substring(1, list[0].length());
        List<Couple<Observation,Integer>> value = null;
        for (int i = 1; i < list.length ; i++){ //each of vale
            String next = list[i];
            System.out.println(" == Traitement : "+ next);
            value = new ArrayList<>(); //liste vide
            String letterFirst = String.valueOf(next.charAt(0));
            String letterSecond = String.valueOf(next.charAt(1));
            if (Objects.equals( ((String) letterFirst + letterSecond) , "[]")) {
                String [] mot = next.split(",");
                base.put(key, value);
                System.out.println("ajout elem dans base, KEY = " + key + " || == VALUE = "+value);
                key = mot[1]; //mot suivant est la prochaine clé
            } else {
                String[] mot = next.split(",");

                //Observation elem1 = Observation.valueOf(mot[0].split("<[")[0]); //mot Gold
                String k = mot[0].substring(2, mot[0].length());
                Observation elem1 = Observation.valueOf( k );
                int value1 = (int) Integer.parseInt(mot[1].split(">")[0]); // capacité gold
                Observation elem2 = Observation.valueOf(mot[2].split("<")[0]); //mot Diamond
                int value2 = (int) Integer.parseInt(mot[3].split(">]")[0]); // capacité diamond

                if (i==list.length -1) {
                    value2 = (int) Integer.parseInt(mot[3].split(">]}")[0]); // capacité diamond
                    key = mot[4]; //mot suivant est la prochaine clé
                }
                value.add(new Couple<>(elem1, value1));
                value.add(new Couple<>(elem2, value2));

                base.put(key, value);
                System.out.println("ajout elem dans base, key : " + key + " || value : "+value);
            }

        }


        return base;
    }
}
