package com.fivetuat.fsoftprojectserver.configurations;

import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.DeckShare;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.QuizDetail;
import com.fivetuat.fsoftprojectserver.entities.QuizResult;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import com.fivetuat.fsoftprojectserver.entities.UserActivity;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import com.fivetuat.fsoftprojectserver.entities.UserStreak;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.QuestionType;
import com.fivetuat.fsoftprojectserver.enums.QuizScope;
import com.fivetuat.fsoftprojectserver.enums.Roles;
import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import com.fivetuat.fsoftprojectserver.enums.SrsStatus;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckShareRepository;
import com.fivetuat.fsoftprojectserver.repositories.QuizResultRepository;
import com.fivetuat.fsoftprojectserver.repositories.TagRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserActivityRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserCardSrsRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserStreakRepository;
import com.fivetuat.fsoftprojectserver.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Init data để test — seed nhiều user + Tag/Deck/Card + dữ liệu ôn tập
 * (SRS), streak, quiz để có sẵn data chạy thử toàn bộ API mà không cần
 * tạo tay qua Postman/Swagger.
 * <p>
 * Chỉ seed 1 lần: nếu email user1@test.com đã tồn tại thì bỏ qua toàn bộ,
 * restart app nhiều lần không bị tạo trùng dữ liệu.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // chạy sau ApplicationInitConfig để đảm bảo Role USER đã tồn tại
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TestDataInitConfig implements ApplicationRunner {

    private static final String TEST_PASSWORD = "Test@123";

    AuthRepository authRepository;
    DeckRepository deckRepository;
    CardRepository cardRepository;
    TagRepository tagRepository;
    DeckShareRepository deckShareRepository;
    UserCardSrsRepository userCardSrsRepository;
    UserStreakRepository userStreakRepository;
    QuizResultRepository quizResultRepository;
    UserActivityRepository userActivityRepository;
    RoleService roleService;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private record CardSeed(String word, String phonetic, String pos, String meaning,
                            String example, String exampleMeaning) {
    }

    @Override
    public void run(ApplicationArguments args) {
        if (authRepository.findByEmail("user1@test.com").isPresent()) {
            log.info("Test data already seeded, skip.");
            return;
        }
        log.info("Seeding test data...");

        Tag family = createTagIfNotExists("Gia đình", "gia-dinh");
        Tag travel = createTagIfNotExists("Du lịch", "du-lich");
        Tag work = createTagIfNotExists("Công việc", "cong-viec");
        Tag food = createTagIfNotExists("Ẩm thực", "am-thuc");

        // 3 user test, phục vụ test phân quyền / chia sẻ deck
        Profile user1 = createUser("user1@test.com", "Nguyễn Văn A");
        Profile user2 = createUser("user2@test.com", "Trần Thị B");
        Profile user3 = createUser("user3@test.com", "Lê Văn C");

        Deck familyDeck = createDeck(user1, "Từ vựng Gia đình cơ bản",
                "Các từ vựng tiếng Anh thông dụng về chủ đề gia đình",
                DeckVisibility.PUBLIC, Set.of(family), List.of(
                        new CardSeed("mother", "/ˈmʌðər/", "n", "mẹ",
                                "She is a loving mother.", "Cô ấy là một người mẹ yêu thương."),
                        new CardSeed("father", "/ˈfɑːðər/", "n", "bố",
                                "My father works as an engineer.", "Bố tôi làm kỹ sư."),
                        new CardSeed("sibling", "/ˈsɪblɪŋ/", "n", "anh chị em ruột",
                                "I have two siblings.", "Tôi có hai anh chị em ruột."),
                        new CardSeed("grandparent", "/ˈɡrænpeərənt/", "n", "ông/bà",
                                "My grandparents live in the countryside.", "Ông bà tôi sống ở quê."),
                        new CardSeed("cousin", "/ˈkʌzən/", "n", "anh chị em họ",
                                "My cousin is visiting this weekend.", "Anh họ tôi sẽ đến chơi cuối tuần này.")
                ));

        Deck travelDeck = createDeck(user1, "Từ vựng Du lịch",
                "Các từ vựng tiếng Anh cần thiết khi đi du lịch",
                DeckVisibility.PUBLIC, Set.of(travel), List.of(
                        new CardSeed("airport", "/ˈeərpɔːrt/", "n", "sân bay",
                                "We arrived at the airport two hours early.", "Chúng tôi đến sân bay sớm hai tiếng."),
                        new CardSeed("passport", "/ˈpæspɔːrt/", "n", "hộ chiếu",
                                "Don't forget to bring your passport.", "Đừng quên mang theo hộ chiếu."),
                        new CardSeed("luggage", "/ˈlʌɡɪdʒ/", "n", "hành lý",
                                "Please keep an eye on your luggage.", "Hãy để ý hành lý của bạn."),
                        new CardSeed("itinerary", "/aɪˈtɪnəreri/", "n", "lịch trình",
                                "Here is our travel itinerary for the trip.", "Đây là lịch trình du lịch của chúng ta."),
                        new CardSeed("reservation", "/ˌrezərˈveɪʃn/", "n", "đặt chỗ trước",
                                "I made a reservation at the hotel.", "Tôi đã đặt chỗ trước ở khách sạn.")
                ));

        Deck workDeck = createDeck(user2, "Từ vựng Công việc văn phòng",
                "Các từ vựng tiếng Anh dùng trong môi trường công sở",
                DeckVisibility.PUBLIC, Set.of(work), List.of(
                        new CardSeed("meeting", "/ˈmiːtɪŋ/", "n", "cuộc họp",
                                "We have a meeting at 9 AM.", "Chúng tôi có cuộc họp lúc 9 giờ sáng."),
                        new CardSeed("deadline", "/ˈdedlaɪn/", "n", "hạn chót",
                                "The deadline for this report is Friday.", "Hạn chót cho báo cáo này là thứ Sáu."),
                        new CardSeed("colleague", "/ˈkɒliːɡ/", "n", "đồng nghiệp",
                                "My colleague helped me finish the task.", "Đồng nghiệp của tôi đã giúp tôi hoàn thành việc."),
                        new CardSeed("promotion", "/prəˈmoʊʃn/", "n", "sự thăng chức",
                                "She got a promotion last month.", "Cô ấy được thăng chức tháng trước."),
                        new CardSeed("salary", "/ˈsæləri/", "n", "lương",
                                "The salary is paid at the end of the month.", "Lương được trả vào cuối tháng."),
                        new CardSeed("resign", "/rɪˈzaɪn/", "v", "từ chức",
                                "He decided to resign from his position.", "Anh ấy quyết định từ chức.")
                ));

        // Deck PRIVATE - dùng để test quyền riêng tư (chỉ chủ deck xem được)
        Deck foodDeck = createDeck(user3, "Từ vựng Ẩm thực",
                "Từ vựng cá nhân về món ăn, chưa công khai",
                DeckVisibility.PRIVATE, Set.of(food), List.of(
                        new CardSeed("appetizer", "/ˈæpɪtaɪzər/", "n", "món khai vị",
                                "We ordered an appetizer before the main course.", "Chúng tôi gọi món khai vị trước món chính."),
                        new CardSeed("dessert", "/dɪˈzɜːrt/", "n", "món tráng miệng",
                                "Ice cream is my favorite dessert.", "Kem là món tráng miệng yêu thích của tôi."),
                        new CardSeed("recipe", "/ˈresəpi/", "n", "công thức nấu ăn",
                                "She shared her recipe for pho.", "Cô ấy đã chia sẻ công thức nấu phở."),
                        new CardSeed("ingredient", "/ɪnˈɡriːdiənt/", "n", "nguyên liệu",
                                "Fresh ingredients make a big difference.", "Nguyên liệu tươi tạo ra sự khác biệt lớn."),
                        new CardSeed("flavor", "/ˈfleɪvər/", "n", "hương vị",
                                "This dish has a spicy flavor.", "Món này có hương vị cay.")
                ));

        // Deck Du lịch (PUBLIC, của user1) được share cho user2 quyền VIEW
        shareDeck(travelDeck, user2, SharePermission.VIEW);

        // Dữ liệu ôn tập (SRS) cho user1 trên deck Gia đình - đủ 4 trạng thái
        seedSrsForFamilyDeck(user1, familyDeck);

        // Streak
        seedStreak(user1, 5, 12, LocalDate.now());
        seedStreak(user2, 1, 3, LocalDate.now().minusDays(1));
        seedStreak(user3, 0, 0, null);

        // Quiz result mẫu cho user1 trên deck Gia đình
        seedQuizResult(user1, familyDeck);

        // Hoạt động 3 ngày gần nhất của user1
        seedUserActivity(user1);

        log.info("Seed test data done. 3 accounts, password chung: {}", TEST_PASSWORD);
        log.info(" - user1@test.com (Nguyễn Văn A): owns familyDeck (PUBLIC) + travelDeck (PUBLIC, shared to user2)");
        log.info(" - user2@test.com (Trần Thị B): owns workDeck (PUBLIC), has view access to user1's travelDeck");
        log.info(" - user3@test.com (Lê Văn C): owns foodDeck (PRIVATE)");
    }

    private Tag createTagIfNotExists(String name, String slug) {
        return tagRepository.findBySlug(slug).orElseGet(() -> tagRepository.save(
                Tag.builder().name(name).slug(slug).build()
        ));
    }

    private Profile createUser(String email, String fullName) {
        Auth auth = Auth.builder()
                .email(email)
                .phone("09" + String.format("%08d", Math.abs(email.hashCode() % 100000000)))
                .passwordHash(bCryptPasswordEncoder.encode(TEST_PASSWORD))
                .build();

        Profile profile = Profile.builder()
                .fullName(fullName)
                .build();
        profile.setAuth(auth);
        auth.setProfile(profile);

        Set<com.fivetuat.fsoftprojectserver.entities.authentication.Role> roles = new HashSet<>();
        roles.add(roleService.getRoleByName(Roles.USER.name()));
        auth.setRoles(roles);

        Auth saved = authRepository.save(auth);
        return saved.getProfile();
    }

    private Deck createDeck(Profile owner, String title, String description,
                            DeckVisibility visibility, Set<Tag> tags, List<CardSeed> cardSeeds) {
        Deck deck = Deck.builder()
                .profile(owner)
                .title(title)
                .description(description)
                .sourceLanguage("en")
                .targetLanguage("vi")
                .visibility(visibility)
                .isOfficial(false)
                .forkCount(0)
                .isDeleted(false)
                .tags(tags)
                .build();

        List<Card> cards = new ArrayList<>();
        int position = 1;
        for (CardSeed seed : cardSeeds) {
            cards.add(Card.builder()
                    .deck(deck)
                    .word(seed.word())
                    .phonetic(seed.phonetic())
                    .partOfSpeech(seed.pos())
                    .meaning(seed.meaning())
                    .exampleSentence(seed.example())
                    .exampleMeaning(seed.exampleMeaning())
                    .position(position++)
                    .build());
        }
        deck.setCards(cards);
        deck.setTotalCards(cards.size());
        return deckRepository.save(deck);
    }

    private void shareDeck(Deck deck, Profile toProfile, SharePermission permission) {
        DeckShare share = DeckShare.builder()
                .deck(deck)
                .profile(toProfile)
                .permission(permission)
                .build();
        deckShareRepository.save(share);
    }

    private void seedSrsForFamilyDeck(Profile owner, Deck deck) {
        List<Card> cards = deck.getCards();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // card 0 (mother): NEW - chưa ôn lần nào, đến hạn ngay hôm nay
        userCardSrsRepository.save(UserCardSrs.builder()
                .profile(owner).card(cards.get(0))
                .status(SrsStatus.NEW).easinessFactor(2.5f).repetitions(0).interval(0)
                .lastReviewedAt(null).nextReviewDate(today)
                .build());

        // card 1 (father): LEARNING - vừa học hôm qua
        userCardSrsRepository.save(UserCardSrs.builder()
                .profile(owner).card(cards.get(1))
                .status(SrsStatus.LEARNING).easinessFactor(2.3f).repetitions(1).interval(1)
                .lastReviewedAt(now.minusHours(18)).nextReviewDate(today)
                .build());

        // card 2 (sibling): REVIEW - đang ôn tốt, chưa đến hạn
        userCardSrsRepository.save(UserCardSrs.builder()
                .profile(owner).card(cards.get(2))
                .status(SrsStatus.REVIEW).easinessFactor(2.6f).repetitions(3).interval(6)
                .lastReviewedAt(now.minusDays(6)).nextReviewDate(today.plusDays(3))
                .build());

        // card 3 (grandparent): REVIEW - đã ôn nhiều lần, interval dài
        userCardSrsRepository.save(UserCardSrs.builder()
                .profile(owner).card(cards.get(3))
                .status(SrsStatus.REVIEW).easinessFactor(2.5f).repetitions(5).interval(14)
                .lastReviewedAt(now.minusDays(14)).nextReviewDate(today.plusDays(7))
                .build());

        // card 4 (cousin): LAPSED - quá hạn ôn 2 ngày, dùng test luồng "card quá hạn"
        userCardSrsRepository.save(UserCardSrs.builder()
                .profile(owner).card(cards.get(4))
                .status(SrsStatus.LAPSED).easinessFactor(1.8f).repetitions(0).interval(1)
                .lastReviewedAt(now.minusDays(10)).nextReviewDate(today.minusDays(2))
                .build());
    }

    private void seedStreak(Profile owner, int current, int longest, LocalDate lastActiveDate) {
        userStreakRepository.save(UserStreak.builder()
                .profile(owner)
                .currentStreak(current)
                .longestStreak(longest)
                .lastActiveDate(lastActiveDate)
                .build());
    }

    private void seedQuizResult(Profile owner, Deck deck) {
        List<Card> cards = deck.getCards();

        QuizResult quizResult = QuizResult.builder()
                .profile(owner)
                .scope(QuizScope.DECK)
                .totalQuestions(5)
                .correctAnswers(4)
                .score(80f)
                .accuracyRate(0.8f)
                .timeSpentSeconds(180)
                .build();

        List<QuizDetail> details = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            boolean isCorrect = i != 4; // sai câu cuối để khớp correctAnswers = 4
            details.add(QuizDetail.builder()
                    .quizResult(quizResult)
                    .card(card)
                    .questionType(QuestionType.MULTIPLE_CHOICE)
                    .userAnswer(isCorrect ? card.getMeaning() : "sai")
                    .correctAnswer(card.getMeaning())
                    .isCorrect(isCorrect)
                    .scoreEarned(isCorrect ? 20f : 0f)
                    .build());
        }
        quizResult.setQuizDetails(details);
        quizResultRepository.save(quizResult);
    }

    private void seedUserActivity(Profile owner) {
        LocalDate today = LocalDate.now();
        userActivityRepository.save(UserActivity.builder()
                .profile(owner).activityDate(today)
                .cardsReviewedCount(5).quizzesCompletedCount(1)
                .build());
        userActivityRepository.save(UserActivity.builder()
                .profile(owner).activityDate(today.minusDays(1))
                .cardsReviewedCount(8).quizzesCompletedCount(0)
                .build());
        userActivityRepository.save(UserActivity.builder()
                .profile(owner).activityDate(today.minusDays(2))
                .cardsReviewedCount(3).quizzesCompletedCount(1)
                .build());
    }
}