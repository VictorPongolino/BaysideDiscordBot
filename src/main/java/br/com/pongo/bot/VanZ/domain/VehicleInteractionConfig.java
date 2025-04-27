package br.com.pongo.bot.VanZ.domain;

import discord4j.core.object.reaction.ReactionEmoji;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig.MeetUpInteractions.*;


@Log4j2
@Setter
@Component
@ConfigurationProperties(prefix = "interaction")
public class VehicleInteractionConfig {

    private Meeting meeting;
    private Map<MeetUpInteractions, MeetingDetails> meetings;

    @PostConstruct
    void init() {
        Objects.requireNonNull(meeting);

        meeting.onSite.setUnicodeRaw(ReactionEmoji.unicode(meeting.onSite.getUnicodeEmoji()).getRaw());
        meeting.bayside.setUnicodeRaw(ReactionEmoji.unicode(meeting.bayside.getUnicodeEmoji()).getRaw());
        meeting.none.setUnicodeRaw(ReactionEmoji.unicode(meeting.none.getUnicodeEmoji()).getRaw());

        this.meetings = Map.of(
                BAYSIDE, meeting.bayside,
                ON_SITE, meeting.onSite,
                NONE, meeting.none
        );
    }

    public MeetingDetails getMeetingInteractionFor(final MeetUpInteractions meetUpInteractions) {
        return meetings.get(meetUpInteractions);
    }

    public Optional<MeetingDetails> getMeetingDetailsForEmoji(final String unicodeEmoji) {
        return meetings.values()
                .stream()
                .filter(meetingDetails -> meetingDetails.unicodeEmoji.equalsIgnoreCase(unicodeEmoji))
                .findFirst();
    }

    public MeetingDetails getOnsiteMeeting() {
        return meeting.onSite;
    }

    public MeetingDetails getBaysideMeeting() {
        return meeting.bayside;
    }

    public MeetingDetails getNoneMeeting() {
        return meeting.none;
    }

    @Getter @Setter
    public static class Meeting {
        private MeetingDetails onSite;
        private MeetingDetails bayside;
        private MeetingDetails none;
    }

    @Setter @Getter
    public static class MeetingDetails {
        private String unicodeEmoji;
        private String unicodeRaw;
    }

    @Getter
    public enum MeetUpInteractions {
        ON_SITE("on-site"),
        BAYSIDE("bayside"),
        NONE("none");

        private final String key;

        MeetUpInteractions(final String key) {
            this.key = key;
        }

        public static Optional<MeetUpInteractions> findByKey(final String key) {
            return Stream.of(values())
                    .filter(constant ->
                            constant.name().equalsIgnoreCase(key)).findFirst();
        }
    }
}
