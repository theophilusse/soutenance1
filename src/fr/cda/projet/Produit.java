package fr.cda.projet;

/**
 * Classe de definition d'un produit du stock.
 */
public class Produit
{
    // Les caracteristiques d'un Produit
    //
    private String  reference;      // reference du produit
    private String  nom;            // nom du produit
    private double  prix;           // prix du produit
    private int     quantite;       // quantit- du produit

    /**
     * Instantiates a new Produit.
     *
     * @param reference the reference
     * @param nom       the nom
     * @param prix      the prix
     * @param quantite  the quantite
     */
    public Produit(String reference,
                   String nom,
                   double prix,
                   int quantite)
    {
        this.reference = reference;
        this.nom = nom;
        this.prix = Math.abs(prix);
        this.quantite = Math.abs(quantite);
    }

    /**
     * Accesseur reference.
     *
     * @return the reference
     */
    public String       getReference()
    {
        return (reference);
    }

    /**
     * Accesseur quantite.
     *
     * @return the quantite
     */
    public int          getQuantite()
    {
        return (quantite);
    }

    /**
     * Setteur prix
     *
     * @param prix the prix
     */
    public void setPrix(double prix)
    {
        this.prix = prix;
    }

    /**
     * Accesseur prix
     *
     * @return the prix
     */
    public double getPrix()
    {
        return (prix);
    }

    /**
     * Setter quantite
     *
     * @param value the value
     * @return the quantite
     */
    public int          setQuantite(int value)
    {
        int     ret;

        ret = 0;
        quantite = value;
        if (quantite < 0)
        {
            ret = Math.abs(quantite);
            quantite = 0;
        }
        return (ret);
    }

    /** Conversion en chaine
     *
     * @return String lisible pour humain
     */
    public String toString()
    {
        return String.format("%-15s %-50s %3.2f   %3d",reference,nom,prix,quantite);
    }

}