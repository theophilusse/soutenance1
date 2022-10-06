package fr.cda.projet;

import fr.cda.ihm.*;

/**
 * The type Gui mod commande.
 */
public class GUIModCommande implements FormulaireInt
{
    private Site site;  // Le site
    private Commande commande;  // Le site
    private Formulaire parent;

    /**
     * Instantiates a new Gui mod commande.
     *
     * @param parent Formulaire parent
     * @param site the site
     * @param commande la commande a modifier
     */
    public GUIModCommande(Formulaire parent, Site site, Commande commande)
    {
        this.site = site;
        this.commande = commande;
        this.parent = parent;

        // Creation du formulaire
        Formulaire form = new Formulaire("Modifier commande",this,300,300);

        //  Creation des elements de l'IHM
        //
        form.setPosition(25,0);
        for (int i = 0; i < commande.getLength(); i++)
            form.addZoneText(commande.getReference(i), commande.getReference(i), true, String.valueOf(commande.getQuantity(i)), 200, 50);
        form.addLabel("");
        form.addButton("APPLY","Appliquer");
        form.addLabel("");
        form.addButton("RETURN","Annuler");

        // Affichage du formulaire
        form.afficher();
    }

    /**
     * Verifie la validite d'une entree numerique (avec prefix +/- facultatif).
     *
     * @param s La chaine a verifier.
     * @return True si numerique
     */
    private boolean isNumeric(String s)
    {
        int shift;
        if (s.length() == 0)
            return (false);
        shift = s.charAt(0) == '-' || s.charAt(0) == '+' ? 1 : 0;
        for (int i = shift; i < s.length(); i++)
            if (s.charAt(i) < '0' || s.charAt(i) > '9')
                return (false);
        return (true);
    }

    /** Methode appellee quand on clique dans un bouton
     *
     * @param form Le formulaire dans lequel se trouve le bouton
     * @param nomSubmit Le nom du bouton qui a -t- utilis-.
     */
    public void submit(Formulaire form,String nomSubmit)
    {
        String      err;
        String      out;
        String      in;

        err = "";
        out = "";
        // Bouton 'Annuler'.
        //
        if (nomSubmit.equals("RETURN")) {
            form.fermer();
            return;
        }

        // Bouton 'Rechercher'
        //
        if (nomSubmit.equals("APPLY"))
        {
            for (int i = 0; i < commande.getLength(); i++)
            {
                in = form.getValeurChamp(commande.getReference(i));
                if (in.length() == 0 || isNumeric(in) == false)
                    err = "Veuillez entrer une quantite valide.";
                else if (in.equals(String.valueOf(commande.getQuantity(i))) == false)
                {
                    if (in.length() >= 2 && (in.charAt(0) == '-' || in.charAt(0) == '+') && in.charAt(1) >= '0' && in.charAt(1) <= '9')
                        commande.setQty(i, commande.getQuantity(i) + Parser.parseInt(in.substring(1)) * (in.charAt(0) == '-' ? -1 : 1));
                    else
                        commande.setQty(i, Parser.parseInt(in));
                }
            }
            if (err.length() == 0)
            {
                parent.setValeurChamp("RESULTATS", site.listerToutesCommandes());
                form.fermer();
            }
        }

        // Retour
        //
        if (err.length() != 0)
            new GUIAlert("Error", err);
        else if (out.length() > 0)
            form.setValeurChamp("RESULTATS", out);
    }
}