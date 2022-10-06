package fr.cda.ihm;

/**
 * The type Gui alert.
 */
public class GUIAlert implements FormulaireInt {
    private Formulaire f;

    /**
     * Instantiates a new Gui alert.
     *
     * @param title   the title
     * @param message the message
     */
    public GUIAlert(String title, String message)
    {
        f = new Formulaire(title,this, 300,100);

        //  Creation des elements de l'IHM
        f.setPosition(25,0);
        f.addLabel(message);
        f.setPosition(125,70);
        f.addButton("OK","Ok");
        f.afficher();
    }

    /**
     * Ferme la fenetre
     *
     * @param form Le formulaire dans lequel se trouve le bouton
     * @param nomSubmit Le nom du bouton qui a -t- utilis-.
     */
    public void submit(Formulaire form, String nomSubmit)
    {
        f.fermer();
    }
}
