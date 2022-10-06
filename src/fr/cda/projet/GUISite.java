package fr.cda.projet;

import fr.cda.ihm.*;

import java.util.ArrayList;

/**
 * Classe de definition de l'IHM principale du compte
 */
public class GUISite implements FormulaireInt
{
    private Site site;  // Le site

    /**
     * Instantiates a new Gui site.
     *
     * @param site the site
     */
    public GUISite(Site site)
    {
        this.site = site;

        // Creation du formulaire
        Formulaire form = new Formulaire("Site de vente",this,1100,730);
        
        //  Creation des elements de l'IHM
        //
        form.setPosition(25,0);
        form.addLabel("Afficher tous les produits du stock");
        form.addButton("AFF_STOCK","Tous le stock");
        form.addLabel("");

        form.addLabel("Afficher un produit");
        form.addText("REF_PRODUIT","Reference",true,"");
        form.addButton("AFF_PRODUIT","Afficher");
        form.addLabel("");

        form.addLabel("Afficher tous les bons de commande");
        form.addButton("AFF_COMMANDES","Toutes les commandes");
        form.addLabel("");
        form.addText("NUM_COMMANDE","Numero de commande",true,"1");
        form.addButton("MOD_COMMANDE_NUM","Modifier commande");
        form.addLabel("");
        form.addButton("AFF_COMMANDE_NUM","Afficher commande");
        form.addLabel("\n\n");

        form.addButton("ENV_COMMANDES","Livrer");
        form.addLabel("");

        form.addButton("CALCULER_VENTES","Calculer ventes");
        form.addLabel("");

        form.addButton("ADD_PRODUIT","Ajouter produit");
        form.addLabel("");

        form.addButton("FERMER","Fermer");
        form.addLabel("");

        form.setPosition(400,0);
        form.addZoneText("RESULTATS","Resultats",
                            false,
                            "",
                            600,700);
        // Affichage du formulaire
        form.afficher();
    }

    /** Methode appellee quand on clique dans un bouton
     *
     * @param form Le formulaire dans lequel se trouve le bouton
     * @param nomSubmit Le nom du bouton qui a -t- utilis-.
     */
    public void submit(Formulaire form, String nomSubmit)
    {
        int                    num;
        ArrayList<String> list;
        String                 numStr;
        String                 out;
        String                 err;
        Commande               commande;

        num = -1;
        err = "";
        out = "";
        // Affichage de tous les produits du stock
        //
        if (nomSubmit.equals("AFF_STOCK"))
            out = site.listerTousProduits();

        // Afficher toutes les commandes
        //
        else if (nomSubmit.equals("AFF_COMMANDES"))
            out = site.listerToutesCommandes();

        // Affichage d'une
        //
        else if (nomSubmit.equals("AFF_COMMANDE_NUM"))
        {
            numStr = form.getValeurChamp("NUM_COMMANDE");
            if (numStr.length() == 0)
                out = site.listerToutesCommandes();
            else {
                try {
                    num = Integer.parseInt(numStr);
                    if (num < 0)
                        throw new Exception();
                } catch (Exception e) { err = "Valeur incorrecte."; }
                if (err.length() == 0)
                    out = site.listerCommande(num);
            }
        }

        // Affiche d'un produit
        //
        else if (nomSubmit.equals("AFF_PRODUIT"))
        {
            out = site.listerProduit(form.getValeurChamp("REF_PRODUIT"));
            if (out.length() == 0)
                out = "Produit introuvable.";
        }

        // Assurer les commandes
        //
        else if (nomSubmit.equals("ENV_COMMANDES"))
        {
            list = site.toutLivrer();
            if (list != null && list.size() > 0)
            {
                out = "";
                num = 0;
                for (String s : list) {
                    num += Parser.parseInt(s.split(";")[1]);
                    out += s + "\n";
                }
                out += "Total: " + num + " articles non livres.";
            }
            else
                out = "Toutes les commandes ont ete livrees.\n";
        }

        // Calculer solde
        //
        else if (nomSubmit.equals("CALCULER_VENTES"))
            out = "Solde total: " + site.getSolde() + "€";

        // Modifier quantite commande
        //
        else if (nomSubmit.equals("MOD_COMMANDE_NUM")) {
            commande = site.getInvalidCommandByNumber(Parser.parseInt(form.getValeurChamp("NUM_COMMANDE")));
            if (commande == null)
                commande = site.getCommandByNumber(Parser.parseInt(form.getValeurChamp("NUM_COMMANDE")));
            if (commande == null)
                err = "Commande introuvable.";
            else
                new GUIModCommande(form, site, commande);
        }

        else if (nomSubmit.equals("ADD_PRODUIT"))
            new GUIAddProduit(form, site);

        // Fermer la fenetre
        //
        else if (nomSubmit.equals("FERMER"))
        {
            form.fermer();
            return ;
        }

        // Retour
        //
        if (err.length() != 0)
            new GUIAlert("Error", err);
        else if (out.length() > 0)
            form.setValeurChamp("RESULTATS", out);
    }
}