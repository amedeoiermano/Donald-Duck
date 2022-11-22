package com.xayris.donalduck.data;

import com.xayris.donalduck.data.entities.Comic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
* Downloads a comic from the I.N.D.U.C.K.S. database
*/
public class ComicsExplorer {

    static final String BASE_URL = "https://inducks.org/";
    static final String ISSUE_BASE_URL = BASE_URL + "issue.php?c=it%2FPM++";
    static final String ISSUE_NOT_FOUND_ERROR = "Issue not found";

    /**
     * Layout types that will be avoided since they don't contain stories
     * */
    static final String LAYOUT_TYPE_COVER= "cover";
    static final String LAYOUT_TYPE_ARTICLE= "article";
    static final String LAYOUT_TYPE_ILLUSTRATION= "illustration";

    public static void downloadComic(String issueNumber, OnComicDownloadedListener listener)
    {
        Executors.newSingleThreadExecutor().execute(() -> {
            DownloadComicResult result = new DownloadComicResult();
            try
            {
                Comic comic = new Comic();
                comic.setIssue(issueNumber);

                Document doc = Jsoup.connect(ISSUE_BASE_URL + issueNumber).get();

                // checks if comic exists
                if(checkComic(doc)) {
                    // gets comic cover image
                    getComicCoverUrl(comic, doc);

                    // gets comic date
                    getComicDate(comic, doc);
                    // gets comic stories
                    getComicStories(comic, doc);

                    result.setComic(comic);
                    result.setStatus(DownloadComicResult.DownloadComicStatus.Success);
                }
                else
                    result.setStatus(DownloadComicResult.DownloadComicStatus.IssueNotFound);
            }
            catch (Exception ex)
            {
                result.setStatus(DownloadComicResult.DownloadComicStatus.Error);
                result.setError(ex);
            }

            listener.onComicDownloaded(result);
        });
    }

    private static void getComicDate(Comic comic, Document doc) throws ParseException {
        // recovers date
        Element dateElement = doc.select("time").first();
        if (dateElement == null)
            return;
        String datetime = dateElement.attr("datetime");
        // checks date format
        String[] splitted = datetime.split("-");
        String format = splitted.length == 3 ? "yyyy-MM-dd" : "yyyy-MM";
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = formatter.parse(datetime);
        comic.setIssueDate(date);
    }

    /**
     *  Checks if the comic exists
     *  @param doc the HTML document of the I.N.D.U.C.K.S. webpage
     */
    private static boolean checkComic(Document doc) {
        Element subHeader = doc.select(".subHeader").first();
        if (subHeader != null)
        {
            return !subHeader.text().equals(ISSUE_NOT_FOUND_ERROR);
        }
        return true;
    }

    /**
     * Recovers the url of comic cover image
     * @param comic the comic object
     * @param doc the HTML document of the I.N.D.U.C.K.S. webpage
     */
    private static void getComicCoverUrl(Comic comic, Document doc)
    {
        // recovers cover image
        Element cover = doc.select("img").first();
        if (cover == null)
            return;
        String coverSrc = cover.absUrl("src");
        comic.setCoverUrl(coverSrc);
    }

    /**
     * Recovers the comic's stories
     * @param comic the comic object
     * @param doc the HTML document of the I.N.D.U.C.K.S. webpage
     */
    private static void getComicStories(Comic comic, Document doc) throws Exception {

        // recovers stories table
        Element storiesTable = doc.select("table.storyTable").first();
        if (storiesTable == null)
            throw new Exception("Stories table not found.");

        Elements storiesRows = storiesTable.select("tr");
        for (Element row : storiesRows) {
            List<Node> rowCells = row.childNodes();
            if (rowCells.isEmpty())
                continue;

            Node storyCodeCellNode = rowCells.get(0);
            if (storyCodeCellNode instanceof TextNode)
                continue;
            Element storyCodeCellElement = (Element) storyCodeCellNode.childNode(3);
            Element storyTitleCellElement = (Element) rowCells.get(1);
            if (storyTitleCellElement.text().trim().isEmpty())
                continue;
            // gets layout
            Element storyLayoutCellNode = (Element) rowCells.get(2);
            Element storyLayoutElement = storyLayoutCellNode.select(".pagel").first();
            if(storyLayoutElement != null)
            {
                String storyLayout = storyLayoutElement.text();
                if(storyLayout.equals(LAYOUT_TYPE_COVER) || storyLayout.equals(LAYOUT_TYPE_ARTICLE) || storyLayout.equals(LAYOUT_TYPE_ILLUSTRATION))
                    continue;
            }
            comic.addStory(
                    storyCodeCellElement.text().trim(),
                    storyTitleCellElement.text().trim()
            );
        }
        if (comic.getStories() == null || comic.getStories().isEmpty())
            throw new Exception("No stories retrieved.");
    }

    public interface OnComicDownloadedListener {
        void onComicDownloaded(ComicsExplorer.DownloadComicResult result);
    }

    public static class DownloadComicResult
    {
        private com.xayris.donalduck.data.entities.Comic comic;
        private DownloadComicStatus status;
        private Exception error;

        public void setComic(Comic comic) {
            this.comic = comic;
        }

        public com.xayris.donalduck.data.entities.Comic getComic() {
            return comic;
        }

        public void setStatus(DownloadComicStatus status) {
            this.status = status;
        }

        public DownloadComicStatus getStatus(){
            return status;
        }

        public void setError(Exception error) {
            this.error = error;
        }

        public Exception getError() {
            return error;
        }

        public enum DownloadComicStatus {
            Success,
            IssueNotFound,
            Error
        }
    }
}
