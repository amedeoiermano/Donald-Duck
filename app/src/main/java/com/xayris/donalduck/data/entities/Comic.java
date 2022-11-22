package com.xayris.donalduck.data.entities;

import org.jsoup.parser.Parser;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Comic extends RealmObject {
    @PrimaryKey
    private String issue;
    private String coverUrl;
    private RealmList<Story> stories;
    private Date issueDate;

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public void setStories(RealmList<Story> stories) {
        this.stories = stories;
    }

    public RealmList<Story> getStories() {
        return stories;
    }

    public int getStoriesCount() {
        return stories.size();
    }

    public String getIssueDateFormatted() {
        if(issueDate == null)
            return null;
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(issueDate);
        // increments month
        calendar.add(Calendar.MONTH, 2);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        if(month > 0)
            month -= 1;
        String monthLocalized = new DateFormatSymbols().getMonths()[month];
        return monthLocalized + " " + year;
    }

    public void setIssueDate(Date date)
    {
        issueDate = date;
    }

    public void addStory(String storyCode, String storyTitle) {
        if (storyCode == null || storyTitle == null)
            return;
        storyCode = Parser.unescapeEntities(storyCode, false);
        storyTitle = Parser.unescapeEntities(storyTitle, false);
        if (stories == null)
            stories = new RealmList<>();
        else
        {
            // verifies if it's a duplicate story
            String finalStoryCode = storyCode;
            if(stories.stream().anyMatch(story -> story.getCode().equals(finalStoryCode)))
                return;
        }
        Story story = new Story();
        story.setCode(storyCode);
        story.setTitle(storyTitle);
        stories.add(story);
    }

    public int getReadStoriesCount()
    {
        return (int) stories.stream().filter(Story::getIsRead).count();
    }

    public String getStoriesProgressFormatted() {
            return String.format("%s/%s", getReadStoriesCount(), stories.size());
    }

    public Story getNextUnreadStory()
    {
        Optional<Story> nextUnreadStory = stories.stream().filter(story -> !story.getIsRead()).findFirst();
        return nextUnreadStory.orElse(null);
    }
}
