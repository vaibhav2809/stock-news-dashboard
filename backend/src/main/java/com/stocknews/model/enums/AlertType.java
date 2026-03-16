package com.stocknews.model.enums;

/**
 * Defines the types of stock news alerts a user can configure.
 * Each type has a different trigger condition evaluated by the alert scheduler.
 */
public enum AlertType {

    /** Triggers when the number of articles for a symbol exceeds the configured threshold within 24 hours. */
    NEWS_VOLUME,

    /** Triggers when the latest article sentiment for a symbol matches the configured target sentiment. */
    SENTIMENT_CHANGE,

    /** Triggers when any new article appears for the symbol since the alert was last triggered. */
    BREAKING_NEWS
}
