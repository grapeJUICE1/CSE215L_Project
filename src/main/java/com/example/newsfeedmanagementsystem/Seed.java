package com.example.newsfeedmanagementsystem;

import com.example.newsfeedmanagementsystem.model.*;
import com.example.newsfeedmanagementsystem.repository.*;
import com.example.newsfeedmanagementsystem.service.*;
import com.example.newsfeedmanagementsystem.util.Session;

import java.util.*;

public class Seed {
    public static void main(String[] args) throws Exception {
        UserRepository userRepo = new UserRepository();
        ArticleRepository articleRepo = new ArticleRepository();

        AuthService authService = new AuthService(userRepo);

        List<User> users = new ArrayList<>();
        String[][] userData = {
                {"admin1", "admin1pass", "Admin One", "ADMIN"},
                {"admin2", "admin2pass", "Admin Two", "ADMIN"},
                {"j1", "j1pass", "John Journalist", "Journalist"},
                {"j2", "j2pass", "Jane Journalist", "Journalist"},
                {"j3", "j3pass", "Jim Journalist", "Journalist"},
                {"alice", "alicepass", "Alice", "USER"},
                {"bob", "bobpass", "Bob", "USER"},
                {"charlie", "charliepass", "Charlie", "USER"},
                {"diana", "dianapass", "Diana", "USER"},
                {"eve", "evepass", "Eve", "USER"}
        };

        for (String[] data : userData) {
            User u = authService.register(data[0], data[1], data[2], data[3]);
            users.add(u);
        }
        System.out.println("Created " + users.size() + " users.");

        List<Article> articles = new ArrayList<>();
        String[] categories = {"Weather", "Politics", "Sports", "Tech", "Business"};
        String[] titles = {
                "Storm Alert: Heavy Rainfall Expected",
                "Election Results: Surprising Outcome",
                "Championship Final: Underdog Wins",
                "AI Breakthrough: New Model Released",
                "Stock Market Hits Record High",
                "Climate Summit: Leaders Agree on Action",
                "SpaceX Launches New Satellite",
                "Local Festival Draws Thousands"
        };
        String[] contents = {
                "Meteorologists warn of severe thunderstorms and potential flooding in the region. The National Weather Service has issued a flash flood watch for multiple counties. Residents are advised to stay indoors and avoid unnecessary travel. Emergency services are on high alert as the storm system moves eastward, expected to bring up to 4 inches of rain.",
                "In a stunning turn, the incumbent lost to a newcomer with a landslide victory. The election results have sent shockwaves through the political establishment. Analysts attribute the upset to a youth-led movement focused on climate change and economic reform. The new leader promises to rebuild trust in government and address systemic inequalities.",
                "The underdog team pulled off an incredible win in overtime, shocking the favorites. The game was a nail-biter from start to finish, with momentum swinging back and forth. Fans are celebrating the historic victory, which marks the first championship for the franchise in over two decades. The MVP's performance has been hailed as one of the greatest in playoff history.",
                "A new AI model has achieved state-of-the-art results in natural language processing. The model, developed by a leading research lab, outperforms previous benchmarks on multiple tasks. Experts believe this breakthrough could revolutionize chatbots, translation, and content generation. However, concerns remain about ethical implications and potential misuse.",
                "The Dow Jones Industrial Average reached an all-time high, driven by tech stocks. Strong earnings reports and optimistic outlooks from major companies fueled the rally. Investors are closely watching inflation data and Federal Reserve policy signals. Economists warn that the market may be due for a correction, but confidence remains high.",
                "World leaders have committed to reducing carbon emissions by 50% before 2030. The new agreement, signed at the climate summit, includes binding targets and a framework for verification. Environmental groups praise the deal but call for more ambitious action. Developing nations will receive financial aid to transition to renewable energy sources.",
                "SpaceX successfully deployed a new communications satellite into orbit. The launch, which took place from Cape Canaveral, was flawless. The satellite will provide high-speed internet to remote regions. This marks the company's tenth successful mission this year, cementing its position as a leader in space technology.",
                "The annual festival saw record attendance, with performances by local artists. The three-day event featured music, art, and culinary experiences. Organizers say the festival brought a significant boost to the local economy. Plans are already underway for next year's edition, promising an even bigger lineup."
        };

        List<User> publishers = users.stream()
                .filter(User::canPublish)
                .toList();

        Random rand = new Random(42); // fixed seed for reproducibility
        for (int i = 0; i < titles.length; i++) {
            User author = publishers.get(rand.nextInt(publishers.size()));
            String category = categories[rand.nextInt(categories.length)];
            String type = rand.nextBoolean() ? "Breaking News" : "Editorial";

            Article article;
            if (type.equals("Breaking News")) {
                article = new BreakingNews(titles[i], contents[i], author, category);
            } else {
                article = new Editorial(titles[i], contents[i], author, category);
            }
            articleRepo.addArticle(article);
            articles.add(article);
        }
        System.out.println("Created " + articles.size() + " articles.");

        List<User> commenters = users;

        for (Article article : articles) {
            int numComments = 2 + rand.nextInt(4);
            for (int c = 0; c < numComments; c++) {
                User commenter = commenters.get(rand.nextInt(commenters.size()));
                Session.login(commenter);
                String commentText = "Great article! " + UUID.randomUUID().toString().substring(0, 6);
                Comment comment = new Comment(commenter, commentText);
                try {
                    article.addComment(comment);
                } catch (Exception e) {
                    System.err.println("Could not add comment: " + e.getMessage());
                }

                if (rand.nextBoolean()) {
                    User replier = commenters.get(rand.nextInt(commenters.size()));
                    Session.login(replier);
                    String replyText = "Reply: " + UUID.randomUUID().toString().substring(0, 5);
                    Comment reply = new Comment(replier, replyText);
                    try {
                        comment.addComment(reply);
                    } catch (Exception e) {
                        System.err.println("Could not add reply: " + e.getMessage());
                    }
                }
            }
        }

        for (User user : users) {
            Session.login(user);
            List<Article> shuffled = new ArrayList<>(articles);
            Collections.shuffle(shuffled, rand);
            for (int i = 0; i < Math.min(2, shuffled.size()); i++) {
                try {
                    shuffled.get(i).toggleLike();
                } catch (Exception e) {
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            Article art = articles.get(rand.nextInt(articles.size()));
            User reporter = commenters.get(rand.nextInt(commenters.size()));
            Session.login(reporter);
            if (art.report()) {
                System.out.println("Reported: " + art.getTitle());
            }
            User reporter2 = commenters.get(rand.nextInt(commenters.size()));
            Session.login(reporter2);
            if (art.report()) {
                System.out.println("Reported again: " + art.getTitle());
            }
        }

        userRepo.save();
        articleRepo.save();
        System.out.println("💾 Data saved.");

        System.out.println("--- Seed Summary ---");
        System.out.println("Users: " + userRepo.getAllUsers().size());
        System.out.println("Articles: " + articleRepo.getAllArticles().size());
        long totalComments = articleRepo.getAllArticles().stream()
                .flatMap(a -> a.getComments().stream())
                .count();
        System.out.println("Comments: " + totalComments);
        long totalReplies = articleRepo.getAllArticles().stream()
                .flatMap(a -> a.getComments().stream())
                .flatMap(c -> c.getReplies().stream())
                .count();
        System.out.println("Replies: " + totalReplies);
        System.out.println("Seed completed.");
    }
}