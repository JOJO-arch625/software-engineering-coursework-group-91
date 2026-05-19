这个已经被修好了。TA Profile 页面也是用 pageStart.jspf 的外壳（它 include 了 ../fragments/pageStart.jspf），所以刚才修语言切换 URL 的改动同时修复了所有内部页面：

页面	URL	切换语言后
TA Profile	/ta/profile	/ta/profile?lang=zh ✓ 无参数，正常
TA Jobs	/ta/jobs	/ta/jobs?lang=zh ✓
TA Job Detail	/ta/job?id=job-1	/ta/job?id=job-1&lang=zh ✓ 保留 id
MO Job Editor	/mo/jobs/edit?id=job-1	/mo/jobs/edit?id=job-1&lang=zh ✓
MO Review	/mo/review?jobId=...&appId=...	...&lang=zh ✓
因为所有登录后的页面都共用 pageStart.jspf 里的同一个语言切换按钮，修一处全部修复。

你重启一下 Tomcat 就能看到效果：在任何页面（包括 Profile）切换语言，URL 参数不会丢，页面状态不会重置。