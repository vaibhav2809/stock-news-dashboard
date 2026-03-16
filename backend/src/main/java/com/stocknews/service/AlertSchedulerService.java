package com.stocknews.service;

import com.stocknews.model.entity.Alert;
import com.stocknews.model.entity.NewsArticle;
import com.stocknews.model.enums.AlertFrequency;
import com.stocknews.repository.AlertRepository;
import com.stocknews.repository.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Scheduled service that periodically evaluates active alerts and triggers them
 * when their conditions are met. Runs every 5 minutes by default (configurable via
 * {@code app.scheduler.alert-check-interval-ms}).
 *
 * <p>Supports three alert types:</p>
 * <ul>
 *   <li><b>NEWS_VOLUME</b> — triggers when article count for a symbol in the last 24h exceeds the threshold</li>
 *   <li><b>SENTIMENT_CHANGE</b> — triggers when the latest article's sentiment matches the target</li>
 *   <li><b>BREAKING_NEWS</b> — triggers when any new article appears since the alert was last triggered</li>
 * </ul>
 *
 * <p>Respects the alert's {@link AlertFrequency} to enforce cooldown periods between triggers.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class AlertSchedulerService {

    private static final int HOURS_IN_DAY = 24;

    private final AlertRepository alertRepository;
    private final NewsArticleRepository newsArticleRepository;

    /**
     * Evaluates all active alerts on a fixed schedule.
     * For each alert, checks whether the configured condition is met and whether the
     * cooldown period has elapsed since the last trigger. If both conditions are satisfied,
     * marks the alert as triggered and logs the event.
     */
    @Scheduled(fixedRateString = "${app.scheduler.alert-check-interval-ms:300000}")
    @Transactional
    public void checkAlerts() {
        final List<Alert> activeAlerts = alertRepository.findByIsActiveTrue();
        if (activeAlerts.isEmpty()) {
            log.debug("No active alerts to evaluate");
            return;
        }

        log.info("Evaluating {} active alerts", activeAlerts.size());

        for (final Alert alert : activeAlerts) {
            if (isWithinCooldownPeriod(alert)) {
                log.debug("Alert id={} for symbol={} is within cooldown, skipping",
                        alert.getId(), alert.getSymbol());
                continue;
            }

            final boolean isTriggered = evaluateAlert(alert);
            if (isTriggered) {
                alert.setLastTriggeredAt(OffsetDateTime.now());
                alertRepository.save(alert);
                log.info("ALERT TRIGGERED: id={}, userId={}, symbol={}, alertType={}",
                        alert.getId(), alert.getUserId(), alert.getSymbol(), alert.getAlertType());
            }
        }
    }

    /**
     * Evaluates a single alert's condition based on its type.
     *
     * @param alert the alert to evaluate
     * @return true if the alert condition is met and should be triggered
     */
    private boolean evaluateAlert(Alert alert) {
        return switch (alert.getAlertType()) {
            case NEWS_VOLUME -> evaluateNewsVolumeAlert(alert);
            case SENTIMENT_CHANGE -> evaluateSentimentChangeAlert(alert);
            case BREAKING_NEWS -> evaluateBreakingNewsAlert(alert);
        };
    }

    /**
     * Checks if the number of articles for a symbol in the last 24 hours exceeds the configured threshold.
     *
     * @param alert the NEWS_VOLUME alert to evaluate
     * @return true if article count exceeds threshold
     */
    private boolean evaluateNewsVolumeAlert(Alert alert) {
        if (alert.getThresholdValue() == null) {
            log.warn("NEWS_VOLUME alert id={} has no threshold configured, skipping", alert.getId());
            return false;
        }

        final OffsetDateTime twentyFourHoursAgo = OffsetDateTime.now().minusHours(HOURS_IN_DAY);
        final Page<NewsArticle> recentArticles = newsArticleRepository
                .findBySymbolAndPublishedAtBetweenOrderByPublishedAtDesc(
                        alert.getSymbol(), twentyFourHoursAgo, OffsetDateTime.now(), PageRequest.of(0, 1));

        final long articleCount = recentArticles.getTotalElements();
        final boolean isExceeded = articleCount >= alert.getThresholdValue();

        log.debug("NEWS_VOLUME check: alert id={}, symbol={}, articleCount={}, threshold={}, triggered={}",
                alert.getId(), alert.getSymbol(), articleCount, alert.getThresholdValue(), isExceeded);

        return isExceeded;
    }

    /**
     * Checks if the latest article's sentiment for a symbol matches the alert's target sentiment.
     *
     * @param alert the SENTIMENT_CHANGE alert to evaluate
     * @return true if the latest article's sentiment matches the target
     */
    private boolean evaluateSentimentChangeAlert(Alert alert) {
        if (alert.getTargetSentiment() == null) {
            log.warn("SENTIMENT_CHANGE alert id={} has no target sentiment configured, skipping", alert.getId());
            return false;
        }

        final Page<NewsArticle> latestArticlePage = newsArticleRepository
                .findBySymbolOrderByPublishedAtDesc(alert.getSymbol(), PageRequest.of(0, 1));

        if (latestArticlePage.isEmpty()) {
            log.debug("SENTIMENT_CHANGE check: alert id={}, symbol={}, no articles found", alert.getId(), alert.getSymbol());
            return false;
        }

        final NewsArticle latestArticle = latestArticlePage.getContent().getFirst();
        final boolean isMatched = latestArticle.getSentiment() == alert.getTargetSentiment();

        log.debug("SENTIMENT_CHANGE check: alert id={}, symbol={}, latestSentiment={}, targetSentiment={}, triggered={}",
                alert.getId(), alert.getSymbol(), latestArticle.getSentiment(), alert.getTargetSentiment(), isMatched);

        return isMatched;
    }

    /**
     * Checks if any new articles have been published for a symbol since the alert was last triggered.
     * If the alert has never been triggered, checks for any articles published in the last 24 hours.
     *
     * @param alert the BREAKING_NEWS alert to evaluate
     * @return true if new articles exist since the last trigger
     */
    private boolean evaluateBreakingNewsAlert(Alert alert) {
        final OffsetDateTime since = alert.getLastTriggeredAt() != null
                ? alert.getLastTriggeredAt()
                : OffsetDateTime.now().minusHours(HOURS_IN_DAY);

        final Page<NewsArticle> newArticles = newsArticleRepository
                .findBySymbolAndPublishedAtBetweenOrderByPublishedAtDesc(
                        alert.getSymbol(), since, OffsetDateTime.now(), PageRequest.of(0, 1));

        final boolean hasNewArticles = newArticles.getTotalElements() > 0;

        log.debug("BREAKING_NEWS check: alert id={}, symbol={}, since={}, newArticleCount={}, triggered={}",
                alert.getId(), alert.getSymbol(), since, newArticles.getTotalElements(), hasNewArticles);

        return hasNewArticles;
    }

    /**
     * Determines whether an alert is still within its cooldown period based on its frequency.
     * An alert that has never been triggered is never within cooldown.
     *
     * @param alert the alert to check
     * @return true if the alert was triggered recently and should not be re-evaluated yet
     */
    private boolean isWithinCooldownPeriod(Alert alert) {
        if (alert.getLastTriggeredAt() == null) {
            return false;
        }

        final OffsetDateTime cooldownExpiry = switch (alert.getFrequency()) {
            case HOURLY -> alert.getLastTriggeredAt().plusHours(1);
            case DAILY -> alert.getLastTriggeredAt().plusDays(1);
            case WEEKLY -> alert.getLastTriggeredAt().plusWeeks(1);
        };

        return OffsetDateTime.now().isBefore(cooldownExpiry);
    }
}
