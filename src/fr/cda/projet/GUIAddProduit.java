package fr.cda.projet;

import fr.cda.ihm.*;

import java.text.Normalizer;

/**
 * The type Gui add produit.
 */
public class GUIAddProduit implements FormulaireInt
{
    private Site            site;  // Le site
    private Formulaire      parent;
    private static int      refValue = 0;

    /**
     * Instantiates a new Gui add produit.
     *
     * @param parent Formulaire parent
     * @param site the site
     */
    public GUIAddProduit(Formulaire parent, Site site)
    {
        String[]               list;

        this.site = site;
        this.parent = parent;
        // Creation du formulaire
        Formulaire form = new Formulaire("Ajouter produit",this,400,530);

        //  Creation des elements de l'IHM
        //
        form.setPosition(25,0);
        form.addZoneText("REFERENCE", "Reference", true, "REF-" + refValue, 200, 50);
        form.addLabel("");
        form.addZoneText("NOM", "Nom", true, "", 200, 50);
        form.addLabel("");
        form.addZoneText("PRIX", "Prix (€)", true, "", 200, 50);
        form.addLabel("");
        form.addZoneText("QUANTITE", "Quantite", true, "1", 200, 50);
        form.addLabel("");
        form.addButton("APPLY","Ajouter");
        form.addLabel("");
        form.addButton("RETURN","Annuler");

        // Affichage du formulaire
        form.afficher();
    }

    /** Methode appellee quand on clique dans un bouton
     *
     * @param form Le formulaire dans lequel se trouve le bouton
     * @param nomSubmit Le nom du bouton qui a -t- utilis-.
     */
    public void submit(Formulaire form,String nomSubmit)
    {
        GUIAlert    alert;
        String      err;
        String      out;
        String      in;
        String      nom;
        String      reference;
        int         num;
        double      euro;

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
            Produit         p;

            reference = form.getValeurChamp("REFERENCE").toUpperCase();
            if (reference.length() == 0)
                return ;
            p = site.getProduit(reference);
            if (p != null)
                err = "Cette reference existe deja.";
            else
            {
                in = form.getValeurChamp("QUANTITE");
                num = Parser.parseInt(in);
                if (in.length() == 0 || num < 0)
                    err = "Veuillez entrer une quantite valide.";
                else {
                    in = form.getValeurChamp("PRIX");
                    euro = Parser.parsePositiveDouble(in);
                    if (in.length() == 0 || euro < 0)
                        err = "Veuillez entrer un prix valide.";
                    else {
                        nom = form.getValeurChamp("NOM");
                        if (nom.length() == 0)
                            err = "";
                        else {
                            site.addProduit(new Produit(reference, nom, euro, num));
                            refValue++;
                            parent.setValeurChamp("RESULTATS", site.listerTousProduits());
                            form.fermer();
                            return;
                        }
                    }
                }
            }
        }

        // Retour
        //
        if (err.length() != 0)
            alert = new GUIAlert("Error", err);
        else if (out.length() > 0)
            form.setValeurChamp("RESULTATS", out);
    }
}