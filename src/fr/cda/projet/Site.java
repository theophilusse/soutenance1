package fr.cda.projet;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import fr.cda.util.*;

/**
 * Classe de definition du site de vente
 */
public class Site
{
    private ArrayList<Produit> stock;       // Les produits du stock
    private ArrayList<Produit> stockEpuise;       // Les produits du stock
    private ArrayList<Commande> commandes;  // Les bons de commande en cours
    private ArrayList<Commande> commandesHonorees;  // Les bons de commande faits
    private ArrayList<Commande> commandesNonLivrees;  // Les bons de commande faits
    private boolean silent;
    private String dbCommandes;
    private String dbProduits;
    private double solde;

    /**
     * Instantiates a new Site.
     */
// Constructeur
    //
    public Site()
    {
        int         err;

        solde = 0d;
        dbCommandes = "data/Commandes.txt";
        dbProduits = "data/Produits.txt";
        silent = false;
        stock = new ArrayList<Produit>();
        stockEpuise = new ArrayList<Produit>();
        // lecture du fichier data/Produits.txt
        //  pour chaque ligne on cree un Produit que l'on ajoute a stock
        displayDBFailure(initialiserStock(dbProduits), dbProduits, silent);

        commandes = new ArrayList<Commande>();
        //  lecture du fichier data/Commandes.txt
        //  pour chaque ligne on cree une commande ou on ajoute une reference
        //  d'un produit a une commande existante.
        // AC AC
        displayDBFailure(initialiserCommande(dbCommandes), dbCommandes,  silent);
        sortCammandeByDate(commandes);

        commandesHonorees = new ArrayList<Commande>();
        commandesNonLivrees = new ArrayList<Commande>();
    }

    /**
     * Sum price double.
     *
     * @param c the c
     * @return the double
     */
    public double           sumPrice(Commande c)
    {
        double            sum;
        int               pIndex;
        Produit           p;

        sum = 0d;
        for (int i = 0; i < c.getLength(); i++) {
            p = c.getProductByReference(stock, c.getReference(i));
            if (p == null)
                continue;
            sum += c.getQuantity(i) * p.getPrix();
        }
        return (sum);
    }

    /**
     * Swap two entries.
     *
     * @param stock
     * @param indexA
     * @param indexB
     * @return
     */
    private static boolean swap(ArrayList<Commande> stock, int indexA, int indexB)
    {
        Commande        tmp;

        if (indexA == indexB)
            return (true);
        if (indexA < 0 || indexA >= stock.size() || indexB < 0 || indexB <= stock.size())
            return (false);
        tmp = stock.get(indexA);
        stock.set(indexA, stock.get(indexB));
        stock.set(indexB, tmp);
        return (true);
    }

    /**
     * Trie une liste de commandes par anciennete (croissant).
     *
     * @param c Liste de commande.
     */
    public void sortCammandeByDate(ArrayList<Commande> c)
    {
        int               k;
        int               len;
        String            d0;
        String            d1;

        len = c.size() - 1;
        if (len <= 0)
            return ;
        k = -1;
        while (k != 0) {
            k = 0;
            for (int i = 0; i < len - 1; i++) {
                d0 = c.get(i).getDate();
                d1 = c.get(i + 1).getDate();
                if (d0.length() == 0 && d1.length() != 0) {
                    swap(c, i, i + 1);
                    k++;
                    continue;
                }
                if (countChar(d0, '\'') == 2 && countChar(d1, '\'') == 2)
                    for (int j = 2; j >= 0; j--)
                        if (Parser.parseInt(d0.split("/")[j]) < Parser.parseInt(d1.split("/")[j])) {
                            swap(c, i, i + 1);
                            k++;
                            j = 0;
                        }
            }
        }
    }

    /**
     *
     * Calcul la somme des entiers contenu dans une colonne donnee d'un tableau.
     *
     * @param list
     * @param column
     * @return Somme de la colonne
     */
    private int                        sumColumn(ArrayList<String> list, int column)
    {
        int             sum;

        if (list == null || list.size() <= 0 || column < 0 || column >= list.get(0).split(";").length)
            return (0);
        sum = 0;
        for (int i = 0; i < list.size(); i++)
            sum += Parser.parseInt(list.get(i).split(";")[1]);
        return (sum);
    }

    /**
     *
     *
     *
     * @param a
     * @param b
     * @return
     */
    private ArrayList<String>         mergeList(ArrayList<String> a, ArrayList<String> b)
    {
        String                  ref;
        String                  merge;
        String[]                col;

        if (a == null)
            a = new ArrayList<String>();
        if (b == null || (a.size() > 0 && countChar(a.get(0), ';') < 1))
            return (null);
        if (a.size() == 0) // Si liste vide
        {
            for (String s : b)
                a.add(new String(s)); // Ajoute a la premiere liste
            return (a);
        }
        for (int i = 0; i < a.size(); i++) // Pour chaques elements de la liste
        {
            ref = a.get(i).split(";")[0]; // Recupere le refentiel
            for (int j = 0; j < b.size(); j++) { // Pour chaque elements de la secondes liste
                col = b.get(j).split(";"); // Recupere les colonnes
                if (col[0].equals(ref)) // Si la reference correspond
                {
                    merge = ";" + (Parser.parseInt(a.get(i).split(";")[1]) + Parser.parseInt(col[1])); // Ajoute les quantites
                    merge += "" + (col.length >= 2 ? col[2] : ""); // Ajoute les commentaires
                    a.set(i, ref + merge); // Merge
                    b.remove(j); // Todo debug --------------------------
                    break;
                }
            }
        }
        for (int i = 0; i < b.size(); i++) // Enfin, on ajoute tout les elements non present de la liste B.
            a.add(b.get(i));
        return (a);
    }

    /** Duplique une liste de commande
     *
     * @param l
     * @return Liste dupliquee
     */
    private ArrayList<Commande>           duplicateCommandList(ArrayList<Commande> l)
    {
        ArrayList<Commande>       ret;

        ret = new ArrayList<Commande>();
        for (Commande s : l)
            ret.add(s);
        return (ret);
    }

    /**
     * Effectue toutes les commandes dans la limite des stocks disponibles.
     *
     * @return Nombre d'articles non livres
     */
    public ArrayList<String> toutLivrer()
    {
        ArrayList<String>         articlesNonLivre;
        ArrayList<String>         ret;

        articlesNonLivre = livrerCommandes(duplicateCommandList(commandesNonLivrees), commandesHonorees, commandesNonLivrees);
        ret = mergeList(articlesNonLivre, livrerCommandes(commandes, commandesHonorees, commandesNonLivrees));
        sortCammandeByDate(commandesNonLivrees);
        return (ret);
        //return (livrerCommandes(commandes, commandesHonorees, commandesNonLivrees));
    }

    /**
     * Effectue les commandes dans la limite des stocks disponibles.
     *
     * @param commandes           the commandes
     * @param commandesHonorees   the commandes honorees
     * @param commandesNonLivrees the commandes non livrees
     * @return Nombre d'articles non livres
     */
    public ArrayList<String> livrerCommandes(ArrayList<Commande> commandes, ArrayList<Commande> commandesHonorees, ArrayList<Commande> commandesNonLivrees)
    {
        int                     i;
        int                     qty;
        Produit                 p;
        Commande                c;
        Commande                tmp;
        ArrayList<String>[]     ch;
        ArrayList<String>       missTotal;

        if (commandes == null || commandes.size() <= 0)
            return (null);
        ch = new ArrayList[2];
        ch[0] = new ArrayList<String>(); // Commande honorees
        ch[1] = new ArrayList<String>(); // Commande non honorees
        missTotal = new ArrayList<String>(); // Initialisation de la liste d'articles manquants
        i = 0;
        while (i < commandes.size()) {
            c = commandes.get(i); // Pour chaques commandes
            for (int j = 0; j < c.getLength(); j++) {
                p = getProduit(stock, c.getReference(j)); // Pour chaque produits de la commande
                if (p == null) // Si produit inexistant
                {
                    ch[1].add(c.getReference(j) + ";" + c.getQuantity(j) + "; Produit inexistant");
                    continue;
                }
                qty = p.getQuantite() - c.getQuantity(j); // Calcul la difference entre commande stock
                if (qty < 0) // Si pas assez de produit
                {
                    ch[1].add(p.getReference() + ";" + Math.abs(qty) + "; Quantite manquante"); // Ajoute les articles non honores
                    p.setQuantite(0); // Met a jour le stock produit, plus d'article
                    qty = (c.getQuantity(j) + qty);
                    if (qty > 0)
                        ch[0].add(p.getReference() + ";" + qty); // Ajoute les articles honores
                }
                else // Mais si assez de produits
                {
                    ch[0].add(p.getReference() + ";" + qty); // Ajoute les articles honores
                    p.setQuantite(qty); // Met a jour le stock produit
                }
                stock.set(getReferenceIndex(stock, p.getReference()), p);
                solde += p.getPrix() * (c.getQuantity(j) + qty); // Met a jour le solde
            }
            if (ch[1].size() > 0) { // Si des produits n'ont pas ete livres
                if (commandesHonorees != null && ch[0].size() > 0) {

                    tmp = new Commande(c.getNumber(), c.getDate(), c.getClient());
                    tmp.setList(ch[0]); // Met a jour la commande partiellement honoree dans la liste des commandes honorees.
                    commandesHonorees.add(tmp);
                }
                c.setList(ch[1]); // Ajoute une commande non honoree dans la liste correspondante avec les articles manquants (si elle existe).
                if (commandesNonLivrees != null) {
                    qty = getCommandIndex(commandesNonLivrees, c.getNumber());
                    if (qty == -1)
                        commandesNonLivrees.add(c);
                }
                if (commandesNonLivrees != null || c.getLength() == 0)
                    commandes.remove(i--); // Retire la commande des commandes non traitees.
                mergeList(missTotal, ch[1]);
            }
            else // Mais si tout les produits ont ete livres.
            {
                if (commandesHonorees != null) // Si la liste des commandes honorees existe
                    commandesHonorees.add(c); // La commande est transferee dans les commandes honorees.
                commandes.remove(i--); // Retire la commande des commandes non traitees.
            }
            ch[0].clear();
            ch[1].clear();
            i++;
        }
        //return (sumColumn(missTotal, 1));
        return (missTotal);
    }

    /**
     * Instantiates a new Site.
     *
     * @param silent the silent
     */
    public Site(boolean silent)
    {
        int         err;

        dbCommandes = "data/Commandes.txt";
        dbProduits = "data/Produits.txt";
        this.silent = false;
        stock = new ArrayList<Produit>();
        stockEpuise = new ArrayList<Produit>();
        // lecture du fichier data/Produits.txt
        //  pour chaque ligne on cree un Produit que l'on ajoute a stock
        displayDBFailure(initialiserStock(dbProduits), dbProduits, silent);

        commandes = new ArrayList<Commande>();
        //  lecture du fichier data/Commandes.txt
        //  pour chaque ligne on cree une commande ou on ajoute une reference
        //  d'un produit a une commande existante.
        // AC AC
        displayDBFailure(initialiserCommande(dbCommandes), dbCommandes,  silent);
    }

    /**
     * Sets silent.
     *
     * @param silent the silent
     */
    public void setSilent(boolean silent)
    {
        this.silent = silent;
    }

    /**
     * Is silent boolean.
     *
     * @return the boolean
     */
    public boolean isSilent()
    {
        return (silent);
    }

    /**
     * Display db failure.
     *
     * @param err    the err
     * @param dbName the db name
     * @param silent the silent
     */
    public void         displayDBFailure(int err, String dbName, boolean silent)
    {
        if (silent)
            return ;
        if (err != 0)
        {
            Terminal.ecrireStringln("Erreur detectee dans " + dbName);
            if (err == -1)
                Terminal.ecrireStringln("Fichier introuvable, nouveau fichier.");
            else
                Terminal.ecrireStringln("Fichier corrompu, " + err + " erreurs.");
        }
    }

    /**
     * Liste un produit en particulier.
     *
     * @param ref the ref
     * @return the string
     */
    public String listerProduit(String ref)
    {
        ref = ref.toUpperCase();
        for (Produit p : stock)
            if (p.getReference().equals(ref))
                return (p.toString());
        return ("");
    }

    /**
     * Compte le nombre d'occurences d'un caractere c dans une String s.
     *
     * @param s
     * @param c
     * @return
     */
    private int  countChar(String s, char c)
    {
        int     count;

        count = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == c)
                count++;
        return (count);
    }

    /**
     * Methode qui retourne sous la forme d'une chaine de caractere tous les produits du stock.
     *
     * @return the string
     */
    public String listerTousProduits()
    {
        String res="";
        for(Produit prod : stock)
            res=res+prod.toString()+"\n";
        return res;
    }

    /**
     * Methode qui retourne sous la forme d'une chaine de caractere toutes les commandes.
     *
     * @return the string
     */
    public String listerToutesCommandes()
    {
        String res;

        res = "Commandes A TRAITER: =====================================\n";
        if (commandes.size() <= 0)
            res += "Aucune\n";
        else
            for (Commande c : commandes)
                res += c.toString() + "\n";
        res += "\nCommandes NON HONOREES: ==================================\n";
        if (commandesNonLivrees.size() <= 0)
            res += "Aucune\n";
        else
            for (Commande c : commandesNonLivrees)
                res += c.toString() + "\n";
        res += "\nCommandes HONOREES: ======================================\n";
        if (commandesHonorees.size() <= 0)
            res += "Aucune\n";
        else
            for (Commande c : commandesHonorees)
                res += c.toString() + "\n";
        return (res);
    }

    /**
     * Retourne la commande non honoree de tel numero.
     *
     * @param numero the numero
     * @return the command by number
     */
    public Commande      getInvalidCommandByNumber(int numero)
    {
        for (Commande c : commandesNonLivrees)
            if (c.getNumber() == numero)
                return (c);
        return (null);
    }

    /**
     * Retourne la commande de tel numero.
     *
     * @param numero the numero
     * @return the command by number
     */
    public Commande      getCommandByNumber(int numero)
    {
        for (Commande c : commandes)
            if (c.getNumber() == numero)
                return (c);
        return (null);
    }

    /**
     * Retourne la commande de tel numero, de tout les types de commande.
     *
     * @param numero the numero
     * @return the command by number
     */
    public Commande      getAllPendingCommandByNumber(int numero)
    {
        for (Commande c : commandesNonLivrees)
            if (c.getNumber() == numero)
                return (c);
        for (Commande c : commandes)
            if (c.getNumber() == numero)
                return (c);
        return (null);
    }

    /**
     * Retourne l'index dans la liste de la commande d'un numero donne.
     *
     * @param numero the numero
     * @param commandes command list
     * @return the command index
     */
    public static int           getCommandIndex(ArrayList<Commande> commandes, int numero)
    {
        for (int i = 0; i < commandes.size(); i++)
            if (commandes.get(i).getNumber() == numero)
                return (i);
        return (-1);
    }

    /**
     * Methode qui retourne sous la forme d'une chaine de caractere une commande.
     *
     * @param numero the numero
     * @return the string
     */
    public String listerCommande(int numero)
    {
        Commande    c;

        c = getCommandByNumber(numero);
        if (c == null)
            return ("");
        return (c.toString());
    }

    /**
     * Chargement du fichier de commandes.
     *
     * @param nomFichier
     * @return
     */
    private int initialiserCommande(String nomFichier)
    {
        int            corruption;
        int            cIndex;
        int            numero;
        String         sDate;
        String[]       date;
        String         nom;
        String[]       ref;
        String[]       lignes;

        // Accede au fichier, sinon en cree un ou retourne
        lignes = Terminal.lireFichierTexte(nomFichier);
        if (lignes == null) {
            Terminal.ecrireFichier(nomFichier, new StringBuffer(""));
            return (-1);
        }
        corruption = 0;
        for(String ligne : lignes)
        {
            if (countChar(ligne, ';') != 3) // Fichier corrompu
            {
                corruption++;
                continue;
            }
            String[] champs = ligne.split("[;]",4);
            numero = Parser.parseInt(champs[0]);         // numero de la commande
            cIndex = getCommandIndex(commandes, numero);
            if (cIndex == -1) // Si numero de commande inexistant, nouvelle commande
            {
                date = champs[1].split("/");           // date de la commande. Au format JJ/MM/AAAA
                try {
                    sDate = Date.valueOf(LocalDate.of(Math.abs(Parser.parseInt(date[2])), Math.abs(Parser.parseInt(date[1])), Math.abs(Parser.parseInt(date[0])))).toString();
                } catch (Exception e) {
                    corruption++;
                    sDate = "1/1/1";
                }
                nom = champs[2]; // nom du client
                if (nom.length() == 0)
                {
                    corruption++;
                    nom = "Inconnu";
                }
                commandes.add(new Commande(numero, sDate.replace('-', '/'), nom));
                cIndex = commandes.size() - 1;
            }
            if (countChar(champs[3], '=') != 1) // Ajoute la reference, si pas de quantite, quantite = 1
                commandes.get(cIndex).addReference(champs[3], 1);
            else
            {
                ref = champs[3].split("=");
                numero = Parser.parseInt(ref[1]);
                if (numero < 1)
                {
                    corruption++;
                    numero = Math.abs(numero);
                }
                commandes.get(cIndex).addReference(ref[0], numero);
            }
            System.out.println(ligne);
        }
        return (corruption);
    }

    /**
     * Retourne l'index du produit dans le tableau selon une reference donnee.
     *
     * @param p
     * @param ref
     * @return
     */
    private static int      getReferenceIndex(ArrayList<Produit> p, String ref)
    {
        for (int i = 0; i < p.size(); i++)
            if (p.get(i).getReference().equals(ref))
                return (i);
        return (-1);
    }

    /**
     * Gets produit.
     *
     * @param ref the ref
     * @return the produit
     */
    public Produit      getProduit(String ref)
    {
        for (int i = 0; i < stock.size(); i++)
            if (stock.get(i).getReference().equals(ref))
                return (stock.get(i));
        return (null);
    }

    /**
     * Add produit.
     *
     * @param p the p
     */
    public void         addProduit(Produit p)
    {
        if (p != null)
            stock.add(p);
    }

    /**
     * Retourne le produit correspondant a une reference donnee.
     *
     * @param p
     * @param ref
     * @return
     */
    private static Produit      getProduit(ArrayList<Produit> p, String ref)
    {
        for (int i = 0; i < p.size(); i++)
            if (p.get(i).getReference().equals(ref))
                return (p.get(i));
        return (null);
    }

    /**
     * Chargement du fichier de stock.
     *
     * @param nomFichier
     * @return
     */
    private int initialiserStock(String nomFichier)
    {
        int         corruption;
        int         cIndex;
        String[]    lignes;

        // Accede au fichier, sinon en cree un ou retourne
        lignes = Terminal.lireFichierTexte(nomFichier);
        if (lignes == null) {
            Terminal.ecrireFichier(nomFichier, new StringBuffer(""));
            return (-1);
        }
        corruption = 0;
        for(String ligne : lignes)
            {
                if (countChar(ligne, ';') != 3) // Fichier corrompu : Formatage entree
                {
                    corruption++;
                    continue;
                }
                String[] champs = ligne.split("[;]",4);
                String reference = champs[0];
                cIndex = getReferenceIndex(stock, reference);
                String nom = champs[1];
                double prix = Parser.parsePositiveDouble(champs[2]);
                int quantite = Parser.parseInt(champs[3]);
                if (cIndex >= 0 || quantite < 0 || prix < 0d) // Fichier corrompu : Article en doublon ou mauvaises valeurs numeriques
                {
                    corruption++;
                    if (prix < 0d)
                        continue;
                    if (quantite < 1)
                        quantite = 0;
                }
                System.out.println(ligne);
                if (quantite == 0) {
                    if (getReferenceIndex(stockEpuise, reference) < 0)
                        stockEpuise.add(new Produit(reference, nom, prix, quantite));
                    else
                        corruption++;
                }
                else {
                    if (cIndex >= 0) // Doublons produit
                        stock.get(cIndex).setQuantite(stock.get(cIndex).getQuantite() + quantite);
                    else
                        stock.add(new Produit(reference, nom, prix, quantite));
                }
            }
        return (corruption);
    }

    /**
     * Gets solde.
     *
     * @return the solde
     */
    public double       getSolde()
    {
        return (solde);
    }
}