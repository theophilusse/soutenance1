package fr.cda.projet;
import fr.cda.util.Terminal;

import java.util.*;

/**
 * Classe de definition d'une commande.
 */
public class Commande
{
    // Les caracteristiques d'une commande
    //
    private boolean livre;          // Commande livree ou non
    private int     numero;         // numero de la commande
    private String  date;           // date de la commande. Au format JJ/MM/AAAA
    private String  client;         // nom du client
    private ArrayList<String>     references; // les references des produits de la commande
    private ArrayList<Integer>    qty;


    //TODO vous devez coder le reste (constructeur, methodes ...)

    /**
     * Instantiates a new Commande.
     *
     * @param numero the numero
     * @param date   the date
     * @param client the client
     */
    public Commande(int numero, String date, String client)
    {
        this.numero = numero;
        this.date = date;
        this.client = client;
        references = new ArrayList<String>();
        qty = new ArrayList<Integer>();
    }

    /**
     * Sets qty.
     *
     * @param index the index
     * @param qty   the qty
     */
    public void         setQty(int index, int qty)
    {
        if (index >= references.size() || index < 0)
            return ;
        if (qty <= 0)
        {
            Terminal.ecrireStringln("Qty : " + qty);
            this.qty.remove(index);
            references.remove(index);
        }
        else
            this.qty.set(index, qty);
    }

    /**
     * Sets qty.
     *
     * @param ref the ref
     * @param qty the qty
     */
    public void         setQty(String ref, int qty)
    {
        int i;

        if (qty < 0)
            return ;
        i = -1;
        while (++i < references.size())
            if (references.get(i).equals(ref))
                break;
        if (i == references.size())
            return ;
        this.qty.set(i, qty);
    }

    /**
     * Retourne l'index d'un produit d'une reference donnee
     *
     * @param ref
     * @return L'index du produit
     */
    private int         searchReference(String ref)
    {
        for (int i = 0; i < references.size(); i++)
            if (references.get(i).equals(ref))
                return (i);
        return (-1);
    }

    /**
     * Nombre de produits.
     *
     * @return the length
     */
    public int            getLength()
    {
        return (references.size());
    }

    /**
     * Gets reference.
     *
     * @param index the index
     * @return the reference
     */
    public String         getReference(int index)
    {
        if (index < 0 || index >= references.size())
            return ("");
        return (references.get(index));
    }

    /**
     * Add reference.
     *
     * @param ref the ref
     * @param qty the qty
     */
    public void         addReference(String ref, int qty)
    {
        int     index;

        index = searchReference(ref);
        if (index < 0)
        {
            references.add(ref);
            this.qty.add(qty);
            return ;
        }
        this.qty.set(index, this.qty.get(index) + qty);
    }

    /**
     * Gets product by reference.
     *
     * @param p   the p
     * @param ref the ref
     * @return the product by reference
     */
    public Produit      getProductByReference(ArrayList<Produit> p, String ref)
    {
        for (int i = 0; i < p.size(); i++)
            if (p.get(i).getReference().equals(ref))
                return (p.get(i));
        return (null);
    }

    /**
     * Missing array list.
     *
     * @param stock the stock
     * @return the array list
     */
    public ArrayList<String>      missing(ArrayList<Produit> stock)
    {
        double                  sum;
        int                     miss;
        Produit                 p;
        ArrayList<String>       ret;

        if (stock == null || stock.size() == 0)
            return (null);
        sum = 0;
        ret = new ArrayList<String>();
        for (int i = 0; i < references.size(); i++)
        {
            p = getProductByReference(stock, references.get(i));
            if (p == null)
                ret.add("introuvable;" + qty.get(i));
            else
            {
                miss = p.getQuantite() - qty.size();
                if (miss < 0)
                    ret.add(p.getReference() + ";" + Math.abs(miss));
            }
        }
        ret.add("" + sum);
        return (ret);
    }

    /**
     * Livrable boolean.
     *
     * @param produit the produit
     * @return the boolean
     */
    private boolean     livrable(ArrayList<Produit> produit)
    {
        if (missing(produit).size() > 0)
            return (false);
        return (true);
    }

    /**
     * Fusionne deux commandes
     *
     * @param c Commande a merge
     */
    public void     merge(Commande c)
    {
        int         index;
        String      ref;

        for (int i = 0; i < c.getLength(); i++)
        {
            index = -1;
            ref = c.getReference(i);
            while (++index < references.size())
                if (references.get(index).equals(ref))
                {
                    setQty(index, getQuantity(index) + c.getQuantity(i));
                    break;
                }
            if (index >= references.size())
            {
                references.add(ref);
                qty.add(c.getQuantity(i));
            }

        }
    }

    /**
     * Affiche produit manquants string.
     *
     * @param produit the produit
     * @return the string
     */
    public String       afficheProduitManquants(ArrayList<Produit> produit)
    {
        Produit         p;
        String          ret;
        int             index;

        if (produit == null || produit.size() == 0)
            return ("");
        ret = "";
        for (int i = 0; i < references.size(); i++)
        {
            index = 0;
            while (index < produit.size() && !produit.get(index).getReference().equals(references.get(i)))
                index++;
            p = produit.get(index);
            if (p.getQuantite() < qty.get(i))
                ret += "il manque " + (qty.get(i) - p.getQuantite()) + " " + p.getReference() + "\n";
        }
        return (ret);
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    public String   getClient()
    {
        return (client);
    }

    /**
     * Gets date.
     *
     * @return the date
     */
    public String   getDate()
    {
        if (date.split("/").length != 3)
            return ("");
        return (date);
    }

    /**
     * Gets number.
     *
     * @return the number
     */
    public int      getNumber()
    {
        return (numero);
    }

    /**
     * Gets quantity.
     *
     * @param index the index
     * @return the quantity
     */
    public int      getQuantity(int index)
    {
        if (index < 0 || index >= qty.size())
            return (-1);
        return (qty.get(index));
    }

    /**
     * Sets list.
     *
     * @param newRef the new ref
     */
    public void     setList(ArrayList<String> newRef)
    {
        references.clear();
        qty.clear();
        if (newRef == null || newRef.size() == 0)
            return ;
        for (int i = 0; i < newRef.size(); i++)
        {
            references.add(newRef.get(i).split(";")[0]);
            qty.add(Parser.parseInt(newRef.get(i).split(";")[1]));
        }
    }

    /**
     * Affiche un produit et sa quantite, en tenant compte de l'alignement vertical.
     *
     * @param index
     * @return
     */
    private String  indentProduct(int index)
    {
        int             space;
        String          ref;

        if (index < 0 || index > references.size())
            return ("");
        ref = references.get(index);
        space = 32 - ref.length();
        if (space < 1) {
            space = 1;
            ref = ref.substring(0, 30);
            ref += "~";
        }
        return (ref + " ".repeat(space) + qty.get(index));
    }

    /**
     * Retire un produit de la commande.
     *
     * @param ref La reference du produit a retirer.
     */
    public void         removeReference(String ref)
    {
        int         index;

        if (ref == null)
            return ;
        ref = ref.toUpperCase();
        index = -1;
        while (++index < references.size())
            if (references.get(index).equals(ref))
                break;
        if (index >= references.size())
            return ;
        references.remove(index);
        qty.remove(index);
    }

    /**
     * Retourne la commande au format humain.
     *
     * @return String pour humain.
     */
    public String   toString()
    {
        String      res;

        res = "Commande\t\t:" + numero + "\n";
        res += "\tDate\t\t:" + date + "\n";
        res += "\tClient\t\t:" + client + "\n";
        res += "\tRefProduits :\n";
        for (int i = 0; i < references.size(); i++)
            res += "\t\t" + indentProduct(i) + "\n";
        return (res + "--------------------------------------------\n\n\n");
    }
}