package model;

/**
 * Represents a single investment holding tracked by the user.
 *
 * Stores both the original purchase price and the current market price
 * so gain/loss calculations can be performed without additional DB queries.
 *
 * Calculated fields (derived, not stored in the DB):
 *   totalCost     = shares * buyPrice
 *   currentValue  = shares * currentPrice
 *   gainLoss      = currentValue - totalCost
 *   gainLossPct   = (gainLoss / totalCost) * 100
 */
public class Investment {

    private final int    id;
    private final int    userId;
    private final String name;
    private final String ticker;
    private final String type;
    private final double shares;
    private final double buyPrice;
    private final double currentPrice;
    private final String purchaseDate;
    private final String notes;

    /**
     * Full constructor used by InvestmentDAO when reading rows from the database.
     *
     * @param id            database primary key
     * @param userId        owner's user ID
     * @param name          human-readable name, e.g. "Apple Inc."
     * @param ticker        ticker symbol, e.g. "AAPL" (may be empty for non-listed assets)
     * @param type          asset type: Stock, ETF, Crypto, Bond, Other
     * @param shares        number of units/shares held
     * @param buyPrice      price paid per share at purchase
     * @param currentPrice  current market price per share
     * @param purchaseDate  purchase date as a string (YYYY-MM-DD)
     * @param notes         optional freeform notes
     */
    public Investment(int id, int userId, String name, String ticker, String type,
                      double shares, double buyPrice, double currentPrice,
                      String purchaseDate, String notes) {
        this.id            = id;
        this.userId        = userId;
        this.name          = name;
        this.ticker        = ticker;
        this.type          = type;
        this.shares        = shares;
        this.buyPrice      = buyPrice;
        this.currentPrice  = currentPrice;
        this.purchaseDate  = purchaseDate;
        this.notes         = notes;
    }

    // ----------------------------------------------------------------
    // Stored field getters
    // ----------------------------------------------------------------

    public int    getId()           { return id; }
    public int    getUserId()       { return userId; }
    public String getName()         { return name; }
    public String getTicker()       { return ticker; }
    public String getType()         { return type; }
    public double getShares()       { return shares; }
    public double getBuyPrice()     { return buyPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public String getPurchaseDate() { return purchaseDate; }
    public String getNotes()        { return notes; }

    // ----------------------------------------------------------------
    // Calculated getters
    // ----------------------------------------------------------------

    /** Total amount originally invested: shares × buy price. */
    public double getTotalCost() {
        return shares * buyPrice;
    }

    /** Current market value of the holding: shares × current price. */
    public double getCurrentValue() {
        return shares * currentPrice;
    }

    /** Absolute gain or loss in dollars: currentValue − totalCost. */
    public double getGainLoss() {
        return getCurrentValue() - getTotalCost();
    }

    /**
     * Percentage gain or loss relative to cost.
     * Returns 0 if totalCost is zero to avoid division-by-zero.
     */
    public double getGainLossPct() {
        if (getTotalCost() == 0) return 0;
        return (getGainLoss() / getTotalCost()) * 100;
    }
}
