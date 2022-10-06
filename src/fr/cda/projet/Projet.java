package fr.cda.projet;
import fr.cda.projet.GUISite;
import fr.cda.projet.Site;

/**
 * The type Main.
 */
public class Projet {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        new GUISite(new Site());
    }
}