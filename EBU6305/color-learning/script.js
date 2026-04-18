// Global state attached to window for cross-script access
window.currentLang = localStorage.getItem('lang') || 'EN';
window.fontSizeMultiplier = parseFloat(localStorage.getItem('fontSize')) || 1;
window.highContrast = localStorage.getItem('highContrast') === 'true';

window.translations = {
    EN: {
        title: 'Color Learning',
        nav_home: 'Homepage',
        nav_learning: 'Learning',
        nav_game: 'Game',
        nav_test: 'Test',
        nav_community: 'Community',
        hero_title: 'Learn Color Encoding',
        hero_subtitle: 'Master RGB/CMYK/HSV/YCbCr',
        btn_prev: 'Prev',
        btn_next: 'Next',
        carousel_desc: 'Explore different color models and understand their applications',
        footer_rights: 'All rights reserved.',
        footer_privacy: 'Privacy Policy',
        footer_terms: 'Terms of Use',
        lang_btn: '中文',
        // Accessibility
        acc_title: 'Accessibility Settings',
        acc_high_contrast: 'High Contrast',
        acc_data_policy: 'Ethical Data Use',
        acc_view_policy: 'View Policy',
        btn_close: 'Close',
        data_policy_title: 'Ethical Data Use Policy',
        data_policy_text: 'We respect your privacy. This site uses local storage only to remember your preferences (language, font size). No personal data is tracked or shared with third parties.',
        // Learning page
        learning_title: 'Extensive & Interesting Color Training',
        learning_subtitle: 'Stretch Your Color Knowledge Without Boredom',
        card_rgb_title: 'RGB/CMYK Basics',
        card_rgb_desc: 'Core color model training covering fundamental concepts of additive and subtractive color systems used in digital and print media.',
        card_hsv_title: 'HSV/YCbCr Practical Use',
        card_hsv_desc: 'Real-world application training exploring color spaces used in image processing, video compression, and creative workflows.',
        card_sub_title: 'Sub-pages',
        card_sub_desc: 'Access detailed learning modules covering advanced topics, exercises, and interactive training materials.',
        // Game page
        game_title: 'Highly Interactive Color Games',
        game_subtitle: 'Demonstrate Concepts or Take Quizzes',
        game_concept_title: 'Concept Demo Game',
        game_concept_desc: 'Visualize color mixing rules interactively',
        game_quiz_title: 'Color Quiz Game',
        game_quiz_desc: 'Test your knowledge with quick quizzes',
        game_more: 'More Games →',
        // Community page
        community_title: 'Color Learning Community',
        community_subtitle: 'Learn Together & Compete',
        comm_social_title: 'Social Learning',
        comm_social_desc: 'Join Group Discussions & Share Tips',
        comm_social_link: 'Explore Discussions →',
        comm_profiles_title: 'Profiles',
        comm_profiles_desc: 'View Learner Profiles & Progress',
        comm_profiles_link: 'Browse Profiles →',
        comm_score_title: 'Scoreboard + Star Learners',
        comm_score_desc: 'Check Leaderboard & Top Performers',
        comm_score_link: 'View Scoreboard →',
        comm_sub_title: 'Community Sub-pages:',
        // Test page
        test_title: 'Challenge Your Color Skills',
        test_subtitle: 'Choose a Level and Start Testing',
        level_beginner: 'Beginner',
        level_intermediate: 'Intermediate',
        level_advanced: 'Advanced',
        level_desc_1: 'Basic color recognition and RGB/CMYK fundamentals.',
        level_desc_2: 'HSV transformations and practical color mixing.',
        level_desc_3: 'Advanced YCbCr encoding and complex color theories.',
        level_start: 'Start Test',
        level_duration: 'Duration:',
        level_questions: 'Questions:',
        // Discussions page
        disc_back: '← Back',
        disc_title: 'Group Discussions',
        disc_subtitle: 'Join conversations and share your learning tips',
        disc_filter_all: 'All',
        disc_filter_tips: 'Learning Tips',
        disc_filter_study: 'Study Methods',
        disc_filter_theory: 'Color Theory',
        disc_filter_progress: 'Progress Updates',
        disc_filter_game: 'Game Tips',
        disc_new: 'Start New Discussion',
        disc_view: 'View →',
        // Profiles page
        prof_title: 'Learner Profiles',
        prof_subtitle: 'Explore member profiles and their learning journey',
        prof_filter_expert: 'Expert',
        prof_view: 'View Full Profile →',
        // Scoreboard page
        score_title: 'Global Scoreboard',
        score_subtitle: 'Top Performers and Star Learners',
        score_rank: 'Rank',
        score_name: 'Name',
        score_points: 'Points',
        score_star_title: 'Star Learners',
        score_periods: 'Time Periods',
        score_weekly: 'Weekly Leaderboard',
        score_monthly: 'Monthly Leaderboard',
        score_alltime: 'All-time Leaderboard',
        // Test About page
        test_about_title: 'About Level 1 Test',
        test_about_desc1: 'Our formative assessment system is designed to help you learn through interactive feedback. Unlike traditional tests that only show your final score, our system provides detailed explanations for each answer, helping you understand not just what the correct answer is, but why it\'s correct.',
        test_about_desc2: 'Each question is carefully crafted to test specific aspects of color encoding knowledge, from basic RGB concepts to advanced color science principles. The immediate feedback helps reinforce learning and correct misconceptions right away.',
        test_about_features_title: 'Key Features',
        test_about_feature1: 'Immediate formative feedback after each answer',
        test_about_feature2: 'Detailed explanations for both correct and incorrect answers',
        test_about_feature3: 'Progressive difficulty levels from basic to advanced',
        test_about_feature4: 'Try again option to reinforce learning',
        test_about_feature5: 'Safe exit with confirmation for in-progress tests',
        test_about_guidelines_title: 'Test Guidelines',
        test_about_guideline1: 'Read each question carefully',
        test_about_guideline2: 'Review feedback thoroughly',
        test_about_guideline3: 'Take your time to learn',
        test_about_guideline4: 'Retake tests to improve',
        // Test Question page
        test_q_exit: 'Exit Test',
        test_q_next: 'Next Question',
        test_q_exit_confirm: 'Are you sure?',
        // Common
        btn_reset: 'Reset',
        btn_undo: 'Undo'
    },
    CN: {
        title: '色彩学习',
        nav_home: '首页',
        nav_learning: '学习',
        nav_game: '游戏',
        nav_test: '测试',
        nav_community: '社区',
        hero_title: '学习色彩编码',
        hero_subtitle: '掌握 RGB/CMYK/HSV/YCbCr',
        btn_prev: '上一个',
        btn_next: '下一个',
        carousel_desc: '探索不同的色彩模型并了解它们的应用',
        footer_rights: '保留所有权利。',
        footer_privacy: '隐私政策',
        footer_terms: '使用条款',
        lang_btn: 'English',
        // Accessibility
        acc_title: '无障碍设置',
        acc_high_contrast: '高对比度模式',
        acc_data_policy: '伦理数据政策',
        acc_view_policy: '查看政策',
        btn_close: '关闭',
        data_policy_title: '伦理数据使用政策',
        data_policy_text: '我们尊重您的隐私。本网站仅使用本地存储来记录您的偏好（语言、字体大小）。不会追踪任何个人数据，也不会与第三方分享。',
        // Learning page
        learning_title: '广泛且有趣的色彩训练',
        learning_subtitle: '在不感枯燥的情况下扩展你的色彩知识',
        card_rgb_title: 'RGB/CMYK 基础',
        card_rgb_desc: '核心色彩模型训练，涵盖数字和印刷媒体中使用的加法和减法色彩系统的基本概念。',
        card_hsv_title: 'HSV/YCbCr 实际应用',
        card_hsv_desc: '实际应用训练，探索图像处理、视频压缩和创意工作流中使用的色彩空间。',
        card_sub_title: '子页面',
        card_sub_desc: '访问涵盖高级主题、练习 and 交互式训练材料的详细学习模块。',
        // Game page
        game_title: '高度互动的色彩游戏',
        game_subtitle: '演示概念或进行测验',
        game_concept_title: '概念演示游戏',
        game_concept_desc: '以交互方式可视化色彩混合规则',
        game_quiz_title: '色彩测验游戏',
        game_quiz_desc: '通过快速测验测试你的知识',
        game_more: '更多游戏 →',
        // Community page
        community_title: '色彩学习社区',
        community_subtitle: '共同学习，共同进步',
        comm_social_title: '社交学习',
        comm_social_desc: '加入小组讨论，分享心得',
        comm_social_link: '探索讨论 →',
        comm_profiles_title: '档案',
        comm_profiles_desc: '查看学习者档案与进度',
        comm_profiles_link: '浏览档案 →',
        comm_score_title: '排行榜与明星学习者',
        comm_score_desc: '查看排行榜与顶尖表现者',
        comm_score_link: '查看排行榜 →',
        comm_sub_title: '社区子页面：',
        // Test page
        test_title: '挑战你的色彩技能',
        test_subtitle: '选择一个等级并开始测试',
        level_beginner: '初级',
        level_intermediate: '中级',
        level_advanced: '高级',
        level_desc_1: '基本色彩识别和 RGB/CMYK 基础知识。',
        level_desc_2: 'HSV 转换和实际色彩混合。',
        level_desc_3: '高级 YCbCr 编码和复杂色彩理论。',
        level_start: '开始测试',
        level_duration: '时长：',
        level_questions: '题目：',
        // Discussions page
        disc_back: '← 返回',
        disc_title: '小组讨论',
        disc_subtitle: '加入对话，分享你的学习心得',
        disc_filter_all: '全部',
        disc_filter_tips: '学习建议',
        disc_filter_study: '学习方法',
        disc_filter_theory: '色彩理论',
        disc_filter_progress: '进度更新',
        disc_filter_game: '游戏技巧',
        disc_new: '发起新讨论',
        disc_view: '查看 →',
        // Profiles page
        prof_title: '学习者档案',
        prof_subtitle: '探索成员档案及他们的学习旅程',
        prof_filter_expert: '专家',
        prof_view: '查看完整档案 →',
        // Scoreboard page
        score_title: '全球排行榜',
        score_subtitle: '顶尖表现者与明星学习者',
        score_rank: '排名',
        score_name: '姓名',
        score_points: '积分',
        score_star_title: '明星学习者',
        score_periods: '时间范围',
        score_weekly: '周排行榜',
        score_monthly: '月排行榜',
        score_alltime: '总排行榜',
        // Test About page
        test_about_title: '关于第1级测试',
        test_about_desc1: '我们的形成性评估系统旨在通过互动反馈帮助您学习。与仅显示最终分数的传统测试不同，我们的系统为每个答案提供详细解释，帮助您不仅了解正确答案是什么，还了解它为什么正确。',
        test_about_desc2: '每个问题都经过精心设计，旨在测试颜色编码知识的特定方面，从基本的 RGB 概念到高级颜色科学原理。即时反馈有助于巩固学习并立即纠正误解。',
        test_about_features_title: '主要特点',
        test_about_feature1: '每个答案后即时反馈',
        test_about_feature2: '对正确和错误答案的详细解释',
        test_about_feature3: '从基础到高级的渐进难度级别',
        test_about_feature4: '再次尝试选项以巩固学习',
        test_about_feature5: '带有进行中测试确认的安全退出',
        test_about_guidelines_title: '测试指南',
        test_about_guideline1: '仔细阅读每个问题',
        test_about_guideline2: '彻底审查反馈',
        test_about_guideline3: '花时间学习',
        test_about_guideline4: '重新参加测试以提高',
        // Test Question page
        test_q_exit: '退出测试',
        test_q_next: '下一题',
        test_q_exit_confirm: '你确定要退出吗？',
        // Common
        btn_reset: '重置',
        btn_undo: '撤销'
    }
};

window.historyStack = [];

window.updateMixer = function() {
    const r = document.getElementById('r-range').value;
    const g = document.getElementById('g-range').value;
    const b = document.getElementById('b-range').value;
    
    document.getElementById('r-val').textContent = r;
    document.getElementById('g-val').textContent = g;
    document.getElementById('b-val').textContent = b;
    
    const hex = `#${parseInt(r).toString(16).padStart(2, '0')}${parseInt(g).toString(16).padStart(2, '0')}${parseInt(b).toString(16).padStart(2, '0')}`.toUpperCase();
    
    document.getElementById('mixer-preview').style.backgroundColor = `rgb(${r},${g},${b})`;
    document.getElementById('hex-display').textContent = hex;
    
    // Push to stack for undo (simple debounced-like logic)
    const lastState = window.historyStack[window.historyStack.length - 1];
    if (!lastState || (lastState.r !== r || lastState.g !== g || lastState.b !== b)) {
        window.historyStack.push({r, g, b});
        if (window.historyStack.length > 50) window.historyStack.shift();
    }
};

window.resetMixer = function() {
    if (confirm(window.currentLang === 'EN' ? 'Reset all sliders?' : '重置所有滑块？')) {
        document.getElementById('r-range').value = 128;
        document.getElementById('g-range').value = 128;
        document.getElementById('b-range').value = 128;
        window.updateMixer();
    }
};

window.undoMixer = function() {
    if (window.historyStack.length > 1) {
        window.historyStack.pop(); // Remove current
        const prevState = window.historyStack[window.historyStack.length - 1];
        document.getElementById('r-range').value = prevState.r;
        document.getElementById('g-range').value = prevState.g;
        document.getElementById('b-range').value = prevState.b;
        window.updateMixer();
        window.historyStack.pop(); // Remove the one pushed by updateMixer
    }
};

window.applyLanguage = function() {
    const lang = window.currentLang;
    const trans = window.translations[lang];
    
    // Update all elements with data-i18n attribute
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (trans[key]) {
            el.textContent = trans[key];
        }
    });

    // Update language button text
    const langBtn = document.querySelector('.lang-btn');
    if (langBtn) {
        langBtn.textContent = trans.lang_btn;
    }

    // Special handling for carousel if it exists (on index.html)
    if (typeof window.updateSlide === 'function') {
        window.updateSlide();
    }
};

window.applyFontSize = function() {
    document.documentElement.style.fontSize = `${window.fontSizeMultiplier * 16}px`;
};

window.applyHighContrast = function() {
    if (window.highContrast) {
        document.body.classList.add('high-contrast');
    } else {
        document.body.classList.remove('high-contrast');
    }
    const toggle = document.getElementById('contrast-toggle');
    if (toggle) toggle.checked = window.highContrast;
};

window.toggleLang = function() {
    window.currentLang = window.currentLang === 'EN' ? 'CN' : 'EN';
    localStorage.setItem('lang', window.currentLang);
    window.applyLanguage();
};

window.changeFontSize = function(delta) {
    window.fontSizeMultiplier += delta * 0.1;
    window.fontSizeMultiplier = Math.max(0.8, Math.min(2.0, window.fontSizeMultiplier));
    localStorage.setItem('fontSize', window.fontSizeMultiplier);
    window.applyFontSize();
};

window.toggleHighContrast = function() {
    window.highContrast = !window.highContrast;
    localStorage.setItem('highContrast', window.highContrast);
    window.applyHighContrast();
};

window.toggleAccessibilityMenu = function() {
    const modal = document.getElementById('accessibility-modal');
    if (modal) {
        modal.style.display = modal.style.display === 'block' ? 'none' : 'block';
    }
};

window.showDataPolicy = function() {
    const lang = window.currentLang;
    const trans = window.translations[lang];
    alert(`${trans.data_policy_title}\n\n${trans.data_policy_text}`);
};

// Initialize Breadcrumbs based on current page
window.updateBreadcrumbs = function() {
    const breadcrumbContainer = document.querySelector('.breadcrumb-container');
    const breadcrumbNav = document.querySelector('.breadcrumbs');
    if (!breadcrumbContainer || !breadcrumbNav) return;

    breadcrumbContainer.style.display = 'block'; // Always show for consistency

    const path = window.location.pathname;
    const pageName = path.split('/').pop() || 'index.html';
    
    const lang = window.currentLang;
    const trans = window.translations[lang];

    if (pageName === 'index.html' || pageName === '') {
        breadcrumbNav.innerHTML = `<a href="index.html" data-i18n="nav_home">${trans.nav_home}</a>`;
        return;
    }

    const pages = {
        'learning.html': trans.nav_learning,
        'game.html': trans.nav_game,
        'test.html': trans.nav_test,
        'community.html': trans.nav_community,
        'discussions.html': trans.disc_title,
        'profiles.html': trans.prof_title,
        'scoreboard.html': trans.score_title,
        'test-about.html': trans.nav_test,
        'test-question.html': trans.nav_test
    };

    if (pages[pageName]) {
        breadcrumbNav.innerHTML = `<a href="index.html" data-i18n="nav_home">${trans.nav_home}</a> <span></span> ${pages[pageName]}`;
    }
};

// Initialize as soon as possible and also on DOMContentLoaded
(function init() {
    window.applyLanguage();
    window.applyFontSize();
    window.applyHighContrast();
    window.updateBreadcrumbs();
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            window.applyLanguage();
            window.applyFontSize();
            window.applyHighContrast();
            window.updateBreadcrumbs();
        });
    }
})();


