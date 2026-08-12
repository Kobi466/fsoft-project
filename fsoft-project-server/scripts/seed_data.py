import json
import sys
import time
import requests

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://localhost:8080/fsoft"

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def register_user(email, password, full_name, phone):
    url = f"{BASE_URL}/auth/register"
    payload = {
        "fullName": full_name,
        "email": email,
        "phone": phone,
        "passWord": password
    }
    log(f"Registering user: {email} ({full_name})...")
    try:
        res = requests.post(url, json=payload)
        if res.status_code in [200, 201]:
            log(f"Success registering user {email}", "OK")
        else:
            log(f"Registration response ({res.status_code}): {res.text}", "WARN")
    except Exception as e:
        log(f"Exception registering user {email}: {e}", "ERROR")

def login_user(email, password):
    url = f"{BASE_URL}/auth/login"
    payload = {
        "email": email,
        "password": password
    }
    log(f"Logging in user: {email}...")
    try:
        res = requests.post(url, json=payload)
        if res.status_code == 200:
            data = res.json().get("data", {})
            token_obj = data.get("token", {})
            token = token_obj.get("accessToken") if isinstance(token_obj, dict) else token_obj
            log(f"Login success! Token acquired for {email}", "OK")
            return token
        else:
            log(f"Login failed ({res.status_code}): {res.text}", "ERROR")
            return None
    except Exception as e:
        log(f"Exception logging in: {e}", "ERROR")
        return None

def create_tag(token, tag_name):
    url = f"{BASE_URL}/tags"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {"name": tag_name}
    try:
        res = requests.post(url, headers=headers, json=payload)
        if res.status_code in [200, 201]:
            tag_data = res.json().get("data", {})
            tag_id = tag_data.get("id")
            log(f"Created Tag '{tag_name}' (ID: {tag_id})", "OK")
            return tag_id
        else:
            log(f"Tag creation response ({res.status_code}): {res.text}", "WARN")
            # Try fetching existing tags
            res_get = requests.get(f"{BASE_URL}/tags", headers=headers)
            if res_get.status_code == 200:
                tags = res_get.json().get("data", [])
                for t in tags:
                    if t.get("name") == tag_name:
                        return t.get("id")
            return None
    except Exception as e:
        log(f"Exception creating tag '{tag_name}': {e}", "ERROR")
        return None

def create_deck(token, title, description, tag_ids, source_lang="en", target_lang="vi", visibility="PUBLIC", is_official=True):
    url = f"{BASE_URL}/decks"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "title": title,
        "description": description,
        "sourceLanguage": source_lang,
        "targetLanguage": target_lang,
        "visibility": visibility,
        "isOfficial": is_official,
        "tagIds": tag_ids
    }
    files = {
        "request": (None, json.dumps(payload), "application/json")
    }
    log(f"Creating Deck: '{title}'...")
    try:
        res = requests.post(url, headers=headers, files=files)
        if res.status_code in [200, 201]:
            deck_data = res.json().get("data", {})
            deck_id = deck_data.get("id")
            log(f"Created Deck '{title}' (ID: {deck_id})", "OK")
            return deck_id
        else:
            log(f"Failed to create deck '{title}' ({res.status_code}): {res.text}", "ERROR")
            return None
    except Exception as e:
        log(f"Exception creating deck '{title}': {e}", "ERROR")
        return None

def create_card(token, deck_id, word, phonetic, part_of_speech, meaning, definition_en, example, example_meaning, position):
    url = f"{BASE_URL}/cards/deck/{deck_id}"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "word": word,
        "phonetic": phonetic,
        "partOfSpeech": part_of_speech,
        "meaning": meaning,
        "definitionEn": definition_en,
        "exampleSentence": example,
        "exampleMeaning": example_meaning,
        "position": position,
        "note": "Sample Card Data"
    }
    files = {
        "request": (None, json.dumps(payload), "application/json")
    }
    try:
        res = requests.post(url, headers=headers, files=files)
        if res.status_code in [200, 201]:
            card_data = res.json().get("data", {})
            card_id = card_data.get("id")
            log(f"  + Added Card '{word}' -> '{meaning}' (ID: {card_id})", "OK")
            return card_id
        else:
            log(f"Failed to add card '{word}' ({res.status_code}): {res.text}", "ERROR")
            return None
    except Exception as e:
        log(f"Exception adding card '{word}': {e}", "ERROR")
        return None

def run_seeder():
    log("==========================================")
    log("STARTING DATABASE SEEDING VIA API")
    log("==========================================")
    
    # 1. Register test users
    users = [
        {"email": "dev.lead@fsoft.com", "password": "Password123!", "name": "Nguyên Văn Dev", "phone": "0912345678"},
        {"email": "ielts.teacher@fsoft.com", "password": "Password123!", "name": "Trần Thị IELTS", "phone": "0987654321"},
        {"email": "japanese.learner@fsoft.com", "password": "Password123!", "name": "Lê Văn Sakura", "phone": "0933445566"},
    ]
    
    for u in users:
        register_user(u["email"], u["password"], u["name"], u["phone"])
    
    # 2. Login primary user to seed tags and decks
    token = login_user("dev.lead@fsoft.com", "Password123!")
    if not token:
        log("Cannot continue without token. Exiting.", "ERROR")
        return

    # 3. Create Tags
    tag_names = [
        "IT & Tech Terms",
        "Business English",
        "IELTS Vocabulary",
        "Nhật Ngữ N5-N4",
        "Nghệ Thuật & Văn Hóa"
    ]
    tag_id_map = {}
    for tname in tag_names:
        tid = create_tag(token, tname)
        if tid:
            tag_id_map[tname] = tid

    # 4. Decks and Cards Dataset
    datasets = [
        {
            "title": "Từ Vựng CNTT - Software Development & AI",
            "description": "Các thuật ngữ cốt lõi về cấu trúc dữ liệu, giải thuật, lập trình đối tượng và AI.",
            "tag": "IT & Tech Terms",
            "source_lang": "en",
            "target_lang": "vi",
            "cards": [
                {
                    "word": "Asynchronous",
                    "phonetic": "/eɪˈsɪŋ.krə.nəs/",
                    "partOfSpeech": "adjective",
                    "meaning": "Bất đồng bộ",
                    "definitionEn": "Not existing or occurring at the same time.",
                    "example": "Node.js handles asynchronous operations non-blockingly.",
                    "example_meaning": "Node.js xử lý các thao tác bất đồng bộ theo cơ chế không chặn."
                },
                {
                    "word": "Concurrency",
                    "phonetic": "/kənˈkʌr.ən.si/",
                    "partOfSpeech": "noun",
                    "meaning": "Tính đồng thời",
                    "definitionEn": "The ability of a system to execute multiple tasks simultaneously.",
                    "example": "Java handles concurrency using thread pools.",
                    "example_meaning": "Java xử lý tính đồng thời bằng cách sử dụng thread pool."
                },
                {
                    "word": "Encapsulation",
                    "phonetic": "/ɪnˌkæp.sjəˈleɪ.ʃən/",
                    "partOfSpeech": "noun",
                    "meaning": "Tính đóng gói",
                    "definitionEn": "Bundling data and methods into a single unit or class.",
                    "example": "Encapsulation protects class properties from unauthorized modification.",
                    "example_meaning": "Tính đóng gói bảo vệ các thuộc tính của lớp khỏi sự thay đổi không hợp lệ."
                },
                {
                    "word": "Polymorphism",
                    "phonetic": "/ˌpɒl.iˈmɔː.fɪ.zəm/",
                    "partOfSpeech": "noun",
                    "meaning": "Tính đa hình",
                    "definitionEn": "The ability of an object to take on many forms.",
                    "example": "Method overriding is a classic example of runtime polymorphism.",
                    "example_meaning": "Ghi đè phương thức là một ví dụ điển hình của tính đa hình lúc thực thi."
                },
                {
                    "word": "Idempotent",
                    "phonetic": "/ˌaɪ.dɛmˈpəʊ.tənt/",
                    "partOfSpeech": "adjective",
                    "meaning": "Hằng đẳng",
                    "definitionEn": "Denoting an operation that can be applied multiple times without changing the result.",
                    "example": "HTTP GET and PUT methods are idempotent.",
                    "example_meaning": "Các phương thức HTTP GET và PUT có tính hằng đẳng."
                },
                {
                    "word": "Refactoring",
                    "phonetic": "/riːˈfæk.tər.ɪŋ/",
                    "partOfSpeech": "noun",
                    "meaning": "Tối ưu tái cấu trúc mã nguồn",
                    "definitionEn": "Restructuring existing computer code without changing its external behavior.",
                    "example": "Code refactoring improves readability and maintainability.",
                    "example_meaning": "Tái cấu trúc code giúp cải thiện độ đọc hiểu và tính bảo trì."
                }
            ]
        },
        {
            "title": "Tiếng Anh Giao Tiếp Công Sở - Business English",
            "description": "Từ vựng và thuật ngữ tiếng Anh chuyên nghiệp sử dụng trong văn phòng và đàm phán.",
            "tag": "Business English",
            "source_lang": "en",
            "target_lang": "vi",
            "cards": [
                {
                    "word": "Bandwidth",
                    "phonetic": "/ˈbænd.wɪdθ/",
                    "partOfSpeech": "noun",
                    "meaning": "Khả năng / Năng lực xử lý công việc",
                    "definitionEn": "The capacity to handle work or problems.",
                    "example": "I don't have the bandwidth to take on another project this week.",
                    "example_meaning": "Tôi không có đủ năng lực/sức lực để nhận thêm dự án nào tuần này."
                },
                {
                    "word": "Synergy",
                    "phonetic": "/ˈsɪn.ə.dʒi/",
                    "partOfSpeech": "noun",
                    "meaning": "Sự cộng hưởng hợp tác",
                    "definitionEn": "The interaction of two or more agents so that their combined effect is greater.",
                    "example": "The merger created great synergy between the sales and marketing teams.",
                    "example_meaning": "Cuộc sáp nhập tạo nên sự cộng hưởng tuyệt vời giữa hai đội ngũ."
                },
                {
                    "word": "Deliverable",
                    "phonetic": "/dɪˈlɪv.ər.ə.bəl/",
                    "partOfSpeech": "noun",
                    "meaning": "Sản phẩm bàn giao",
                    "definitionEn": "A tangible or intangible object produced as a result of a project.",
                    "example": "The main deliverable for Q3 is the new mobile dashboard.",
                    "example_meaning": "Sản phẩm bàn giao chính trong quý 3 là bảng điều khiển di động mới."
                },
                {
                    "word": "Pivot",
                    "phonetic": "/ˈpɪv.ət/",
                    "partOfSpeech": "verb",
                    "meaning": "Chuyển hướng chiến lược",
                    "definitionEn": "Change strategy or direction fundamentally.",
                    "example": "The startup decided to pivot to a B2B model.",
                    "example_meaning": "Công ty khởi nghiệp quyết định chuyển hướng sang mô hình B2B."
                },
                {
                    "word": "Milestone",
                    "phonetic": "/ˈmaɪl.stəʊn/",
                    "partOfSpeech": "noun",
                    "meaning": "Cột mốc quan trọng",
                    "definitionEn": "An action or event marking a significant stage in a development.",
                    "example": "Reaching 10,000 active users is a key milestone for us.",
                    "example_meaning": "Đạt 10.000 người dùng active là một cột mốc quan trọng của chúng tôi."
                }
            ]
        },
        {
            "title": "Từ Vựng IELTS High-Scoring Academic Vocabulary",
            "description": "Bộ từ vựng học thuật Band 7.5+ dành cho kỹ năng Writing & Speaking.",
            "tag": "IELTS Vocabulary",
            "source_lang": "en",
            "target_lang": "vi",
            "cards": [
                {
                    "word": "Ameliorate",
                    "phonetic": "/əˈmiː.li.ə.reɪt/",
                    "partOfSpeech": "verb",
                    "meaning": "Cải thiện, làm tốt hơn",
                    "definitionEn": "To make something bad or unsatisfactory better.",
                    "example": "Government policies aimed to ameliorate housing conditions.",
                    "example_meaning": "Các chính sách của chính phủ nhằm cải thiện điều kiện nhà ở."
                },
                {
                    "word": "Ubiquitous",
                    "phonetic": "/juːˈbɪk.wɪ.təs/",
                    "partOfSpeech": "adjective",
                    "meaning": "Có mặt ở khắp nơi",
                    "definitionEn": "Present, appearing, or found everywhere.",
                    "example": "Smartphones have become ubiquitous in modern society.",
                    "example_meaning": "Điện thoại thông minh đã trở nên phổ biến khắp mọi nơi."
                },
                {
                    "word": "Plausible",
                    "phonetic": "/ˈplɔː.zə.bəl/",
                    "partOfSpeech": "adjective",
                    "meaning": "Hợp lý, đáng tin cậy",
                    "definitionEn": "Seeming reasonable or probable.",
                    "example": "Her explanation sounded perfectly plausible.",
                    "example_meaning": "Lời giải thích của cô ấy nghe vô cùng hợp lý."
                },
                {
                    "word": "Exacerbate",
                    "phonetic": "/ɪɡˈzæs.ə.beɪt/",
                    "partOfSpeech": "verb",
                    "meaning": "Làm trầm trọng thêm",
                    "definitionEn": "Make a problem, bad situation, or negative feeling worse.",
                    "example": "Pollution exacerbates respiratory diseases.",
                    "example_meaning": "Ô nhiễm làm trầm trọng thêm các bệnh về đường hô hấp."
                },
                {
                    "word": "Paradigm",
                    "phonetic": "/ˈpær.ə.daɪm/",
                    "partOfSpeech": "noun",
                    "meaning": "Mô hình, kiểu mẫu",
                    "definitionEn": "A typical example or pattern of something; a model.",
                    "example": "The discovery created a new paradigm in medical science.",
                    "example_meaning": "Phát minh này đã tạo ra một mô hình mới trong y khoa."
                }
            ]
        },
        {
            "title": "Tiếng Nhật Cơ Bản - Minna no Nihongo Bài 1-5",
            "description": "Các từ vựng và câu giao tiếp cơ bản nhất trình độ N5.",
            "tag": "Nhật Ngữ N5-N4",
            "source_lang": "ja",
            "target_lang": "vi",
            "cards": [
                {
                    "word": "わたし (Watashi)",
                    "phonetic": "[watashi]",
                    "partOfSpeech": "pronoun",
                    "meaning": "Tôi",
                    "definitionEn": "I / me",
                    "example": "わたしはエンジニアです。",
                    "example_meaning": "Tôi là kỹ sư."
                },
                {
                    "word": "せんせい (Sensei)",
                    "phonetic": "[sensei]",
                    "partOfSpeech": "noun",
                    "meaning": "Thầy / Cô giáo",
                    "definitionEn": "Teacher / Professor",
                    "example": "たなかさんはせんせいです。",
                    "example_meaning": "Anh Tanaka là giáo viên."
                },
                {
                    "word": "べんきょうします (Benkyoushimasu)",
                    "phonetic": "[benkyoushimasu]",
                    "partOfSpeech": "verb",
                    "meaning": "Học tập",
                    "definitionEn": "To study",
                    "example": "まいにちにほんごをべんきょうします。",
                    "example_meaning": "Mỗi ngày tôi đều học tiếng Nhật."
                },
                {
                    "word": "たべます (Tabemasu)",
                    "phonetic": "[tabemasu]",
                    "partOfSpeech": "verb",
                    "meaning": "Ăn",
                    "definitionEn": "To eat",
                    "example": "あさごはんをたべます。",
                    "example_meaning": "Tôi ăn sáng."
                },
                {
                    "word": "ともだち (Tomodachi)",
                    "phonetic": "[tomodachi]",
                    "partOfSpeech": "noun",
                    "meaning": "Bạn bè",
                    "definitionEn": "Friend",
                    "example": "ともだちといっしょにえいがをみます。",
                    "example_meaning": "Tôi xem phim cùng với bạn bè."
                }
            ]
        },
        {
            "title": "Từ Vựng Tiếng Anh Chuyên Ngành Y Học & Đời Sống",
            "description": "Bộ từ vựng chủ đề sức khỏe, y học và lối sống lành mạnh.",
            "tag": "Nghệ Thuật & Văn Hóa",
            "source_lang": "en",
            "target_lang": "vi",
            "cards": [
                {
                    "word": "Immunity",
                    "phonetic": "/ɪˈmjuː.nə.ti/",
                    "partOfSpeech": "noun",
                    "meaning": "Sự miễn dịch",
                    "definitionEn": "The ability of an organism to resist a particular infection.",
                    "example": "Vaccines help build immunity against diseases.",
                    "example_meaning": "Vắc-xin giúp tạo khả năng miễn dịch chống lại bệnh tật."
                },
                {
                    "word": "Symptom",
                    "phonetic": "/ˈsɪmp.təm/",
                    "partOfSpeech": "noun",
                    "meaning": "Triệu chứng",
                    "definitionEn": "A physical or mental feature indicating a condition of disease.",
                    "example": "Fever is a common symptom of a viral infection.",
                    "example_meaning": "Sốt là một triệu chứng phổ biến của nhiễm vi-rút."
                },
                {
                    "word": "Therapy",
                    "phonetic": "/ˈθer.ə.pi/",
                    "partOfSpeech": "noun",
                    "meaning": "Liệu pháp điều trị",
                    "definitionEn": "Treatment intended to relieve or heal a disorder.",
                    "example": "Physical therapy helped him walk again after the accident.",
                    "example_meaning": "Vật lý trị liệu đã giúp anh ấy đi lại được."
                },
                {
                    "word": "Diagnosis",
                    "phonetic": "/ˌdaɪ.əɡˈnəʊ.sɪs/",
                    "partOfSpeech": "noun",
                    "meaning": "Sự chẩn đoán",
                    "definitionEn": "The identification of the nature of an illness or problem.",
                    "example": "Early diagnosis increases the chance of recovery.",
                    "example_meaning": "Chẩn đoán sớm làm tăng cơ hội phục hồi."
                }
            ]
        }
    ]

    for d in datasets:
        tag_id = tag_id_map.get(d["tag"])
        tag_ids = [tag_id] if tag_id else []
        
        deck_id = create_deck(
            token,
            title=d["title"],
            description=d["description"],
            tag_ids=tag_ids,
            source_lang=d["source_lang"],
            target_lang=d["target_lang"],
            visibility="PUBLIC",
            is_official=True
        )
        
        if deck_id:
            for idx, c in enumerate(d["cards"], start=1):
                create_card(
                    token,
                    deck_id,
                    word=c["word"],
                    phonetic=c["phonetic"],
                    part_of_speech=c["partOfSpeech"],
                    meaning=c["meaning"],
                    definition_en=c["definitionEn"],
                    example=c["example"],
                    example_meaning=c["example_meaning"],
                    position=idx
                )

    log("==========================================")
    log("DATABASE SEEDING COMPLETED SUCCESSFULLY!")
    log("==========================================")

if __name__ == "__main__":
    run_seeder()
