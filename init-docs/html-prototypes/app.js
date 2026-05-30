const state = {
  authed: false,
  role: "buyer",
  page: "buyer-dashboard",
  supplierTab: "info",
  prTab: "overview",
  reviewTab: "todo",
  createStep: 1,
  rfqStatus: "报价中",
  contractStatus: "未创建",
  poStatus: "未生成",
  poApprovalStatus: "草稿",
  paymentStatus: "未付款",
  modal: null,
  toast: null,
  filters: {},
  pageHistory: [],
  selectedPr: "PR-202605-001",
  selectedRfq: "RFQ-202605-001",
  selectedContract: null,
  selectedPo: null,
  selectedFieldLibrary: "certificate",
  selectedFormTemplate: "certificate",
  contractStatusByNo: {},
  prForm: {
    isSoleSupplier: "是",
    supplierType: "客户指定供应商",
    procurementType: "业务成本类采购",
    cdpGroupTitle: "CDP集团",
    pnlIncluded: "是",
  },
};

let suppressHashSync = false;

const roleMeta = {
  admin: { label: "采购经理", name: "周敏", initial: "经", portal: "manager" },
  buyer: { label: "采购员", name: "张伟", initial: "采" },
  business: { label: "业务人员", name: "王敏", initial: "业", department: "市场部" },
  supplier: {
    label: "供应商",
    name: "沈家伟",
    initial: "供",
    company: "上海博夷信息技术有限公司",
  },
};

const navs = {
  admin: [
    ["admin-dashboard", "▦", "经理工作台"],
    ["suppliers", "◆", "供应商管理"],
    ["admin-approval", "✓", "审批中心"],
    ["purchase-requests", "☷", "采购申请单"],
    ["admin-rfqs", "≡", "询价单查询"],
    ["admin-contracts", "▤", "合同管理"],
    ["admin-pos", "▥", "履约与付款"],
    ["admin-report-executive", "▧", "采购分析"],
    ["admin-data-settings", "⚙", "数据设置"],
    ["email-logs", "✉", "邮件日志"],
  ],
  buyer: [
    ["buyer-dashboard", "▦", "工作台"],
    ["suppliers", "◆", "供应商管理"],
    ["reviews", "✓", "审批中心"],
    ["purchase-requests", "☷", "采购申请单"],
    ["rfqs", "≡", "询价单管理"],
    ["contracts", "▤", "合同管理"],
    ["pos", "▥", "履约与付款"],
    ["email-logs", "✉", "邮件日志"],
  ],
  business: [
    ["business-prs", "☷", "我的采购申请"],
    ["business-pr-create", "+", "创建采购申请单"],
    ["business-contracts", "▤", "我的合同"],
    ["business-pos", "▥", "我的PO"],
  ],
  supplier: [
    ["supplier-company", "◆", "企业信息"],
    ["supplier-contacts", "👤", "联系人管理"],
    ["supplier-rfqs", "≡", "我的询价单"],
    ["supplier-contracts", "▤", "合同管理"],
  ],
};

const fieldLibraries = {
  pr: {
    name: "采购申请单创建表单字段库",
    object: "PurchaseRequest",
    updated: "2026-05-28",
    desc: "承接业务人员在采购系统发起 PR 的创建字段、条件字段、预算明细和审批回写字段，用于 PR 创建、详情、筛选、RFQ 来源和下游状态回写。",
    fields: [
      ["pr_code", "PR单号", "文本", "STRING", "提交审批时系统生成 / 不可重复", "基础信息"],
      ["topic", "PR主题", "文本", "STRING", "必填 / 支持列表搜索", "基础信息"],
      ["application_date", "申请日期", "日期", "DATE", "默认当前日期", "基础信息"],
      ["requester_name", "申请人", "人员", "STRING", "当前登录业务人员 / 只读", "申请信息"],
      ["request_department", "申请部门", "部门选择", "STRING", "来自组织部门", "申请信息"],
      ["enduser", "Enduser", "人员展示", "STRING", "1.0 外部带出 / 2.0 可配置", "申请信息"],
      ["department_head", "Department Head", "人员展示", "STRING", "1.0 外部带出 / 2.0 可配置", "申请信息"],
      ["requirement_summary", "产品/服务需求", "多行文本", "STRING", "必填 / 建议10-2000字", "需求信息"],
      ["is_sole_supplier", "是否唯一供应商", "单选", "BOOLEAN", "必填 / 是、否", "需求信息"],
      ["supplier_type", "供应商类型", "单选", "STRING", "是否唯一供应商=是时必填", "需求信息"],
      ["attachment_ids", "附件", "文件上传", "FILE", "可多文件 / 遵循PR附件安全规则", "需求信息"],
      ["cost_center", "成本中心", "选择器", "STRING", "必填 / 成本中心主数据", "财务归集"],
      ["social_security_confirm", "社保/代缴确认", "复选框", "BOOLEAN", "必填勾选", "合规确认"],
      ["procurement_type", "采购类型", "单选", "STRING", "公司费用类采购 / 业务成本类采购", "财务归集"],
      ["customer_name", "客户名称", "文本", "STRING", "业务成本类采购时必填", "业务成本"],
      ["cdp_group_title", "CDP公司抬头", "单选", "STRING", "业务成本类采购时必填 / 来自主数据", "业务成本"],
      ["stamped_sales_contract", "上传双盖章销售合同", "文件上传", "FILE", "业务成本类采购时必填", "业务成本"],
      ["crm_contract_or_order_no", "CRM合同编号/订单编号", "文本", "STRING", "业务成本类采购时必填", "业务成本"],
      ["budget_in_project_pnl", "采购预算是否体现在项目利润表中", "单选", "BOOLEAN", "业务成本类采购时必填 / 是、否", "业务成本"],
      ["budget_lines", "预算明细", "明细表", "ARRAY", "至少1行 / 自动汇总预算总额", "预算信息"],
      ["external_approval_no", "Worklife BPM审批单号", "文本", "STRING", "审批回写 / 不覆盖PR号", "系统信息"],
      ["assigned_buyer", "分配采购员", "人员", "STRING", "审批通过后回写", "系统信息"],
    ],
  },
  supplier: {
    name: "供应商企业信息表单字段库",
    object: "Supplier",
    updated: "2026-05-15",
    desc: "供应商主数据字段，用于供应商入驻、企业信息维护、采购员审核和主数据沉淀。",
    fields: [
      ["supplier_code", "供应商ID号", "文本", "STRING", "系统生成 / VD + 4位数字", "基础信息"],
      ["supplier_name", "供应商名称", "文本", "STRING", "必填 / 最大100字", "基础信息"],
      ["legal_representative", "公司法人", "文本", "STRING", "必填", "工商信息"],
      ["registered_date", "注册时间", "日期", "DATE", "必填", "工商信息"],
      ["registered_capital", "注册资金", "金额", "DECIMAL", "必填 / 大于0", "工商信息"],
      ["company_address", "公司地址", "文本", "STRING", "必填 / 最大200字", "工商信息"],
      ["general_taxpayer", "一般纳税人", "单选", "BOOLEAN", "是/否", "税务信息"],
      ["business_scope", "经营范围", "多行文本", "STRING", "最大1000字", "经营信息"],
      ["company_nature", "企业性质", "下拉选择", "STRING", "国营/私营/合资/外资", "经营信息"],
      ["sales_model", "销售模式", "下拉选择", "STRING", "代理商/经销商/服务商/厂商", "经营信息"],
      ["coverage_region", "覆盖区域", "多选", "JSON", "全国/华东/华北等", "经营信息"],
      ["annual_revenue", "本年度营业额", "金额", "DECIMAL", "可选", "经营信息"],
      ["employee_count", "员工人数", "数字", "NUMBER", "整数", "经营信息"],
      ["main_customers", "主力客户", "多行文本", "STRING", "建议不少于3家", "经营信息"],
      ["bank_name", "开户银行名称", "文本", "STRING", "必填", "银行信息"],
      ["bank_account_no", "银行账号", "文本", "STRING", "必填 / 数字", "银行信息"],
    ],
  },
  certificate: {
    name: "供应商证件上传表单字段库",
    object: "Certificate",
    updated: "2026-05-15",
    desc: "供应商和采购员上传证件时使用，支持按证件类型动态展示扩展字段。",
    fields: [
      ["certificate_type", "证件类型", "下拉选择", "STRING", "来自证件类型字典", "基础信息"],
      ["certificate_file", "证件文件", "附件上传", "FILE", "PDF/JPG/PNG，≤10MB", "基础信息"],
      ["valid_from", "有效期开始", "日期", "DATE", "必填", "有效期"],
      ["valid_to", "有效期截止", "日期", "DATE", "晚于有效期开始", "有效期"],
      ["unified_social_credit_code", "统一社会信用代码", "文本", "STRING", "18位社会信用代码", "营业执照扩展"],
      ["registered_address", "注册地址", "文本", "STRING", "最大200字", "营业执照扩展"],
      ["tax_period", "纳税期间", "文本", "STRING", "完税证明扩展", "完税证明扩展"],
      ["tax_authority", "税务机关", "文本", "STRING", "完税证明扩展", "完税证明扩展"],
      ["certificate_license_no", "许可证编号", "文本", "STRING", "最大50字", "许可证扩展"],
      ["issuing_authority", "发证机关", "文本", "STRING", "最大100字", "许可证扩展"],
    ],
  },
  contact: {
    name: "联系人表单字段库",
    object: "Contact",
    updated: "2026-05-12",
    desc: "供应商联系人维护字段，用于业务通知、RFQ邀请和报价提醒；联系人不区分销售/财务类型。",
    fields: [
      ["contact_remark", "联系人备注", "文本", "STRING", "选填，用于记录对接范围", "基础信息"],
      ["contact_name", "姓名", "文本", "STRING", "必填 / 最大50字", "基础信息"],
      ["mobile", "手机号", "文本", "STRING", "中国大陆手机号", "联系方式"],
      ["email", "邮箱", "文本", "STRING", "邮箱格式", "联系方式"],
      ["position", "职务", "文本", "STRING", "可选", "组织信息"],
      ["department", "部门", "文本", "STRING", "可选", "组织信息"],
      ["is_primary", "是否主要联系人", "单选", "BOOLEAN", "每个供应商至少一个主要联系人", "通知规则"],
    ],
  },
};

const formTemplateMeta = {
  pr: { name: "采购申请单创建表单", useSide: "业务人员端 / 采购员端 / 采购经理端", saveMode: "PR主表 + 条件字段 + 状态回写字段", previewAction: "进入PR详情" },
  supplier: { name: "供应商企业信息表单", useSide: "供应商端 + 采购员端", saveMode: "供应商主表 + 变更记录", previewAction: "进入企业信息" },
  certificate: { name: "供应商证件上传表单", useSide: "供应商端 + 采购员端", saveMode: "证件主表 + 扩展字段", previewAction: "进入证件上传" },
  contact: { name: "联系人表单", useSide: "供应商端 + 采购员端", saveMode: "联系人主表", previewAction: "进入联系人管理" },
};

const suppliers = [
  {
    id: "VD 0001",
    name: "上海博夷信息技术有限公司",
    status: "合作中",
    contact: "沈家伟",
    phone: "13321832330",
    cert: "正常",
    buyer: "张伟",
    updated: "2026-05-04",
  },
  {
    id: "VD 0005",
    name: "北京远景会务服务有限公司",
    status: "合作中",
    contact: "赵强",
    phone: "13801001001",
    cert: "待上传",
    buyer: "张伟",
    updated: "2026-05-10",
  },
  {
    id: "VD 0002",
    name: "山西德利仁信息技术服务有限公司",
    status: "待审核信息",
    contact: "高阳阳",
    phone: "13301126143",
    cert: "正常",
    buyer: "张伟",
    updated: "2026-05-05",
  },
  {
    id: "VD 0003",
    name: "厦门玉树陶朱文化发展有限公司",
    status: "待完善信息",
    contact: "沈荣峰",
    phone: "13599533596",
    cert: "即将到期",
    buyer: "李娜",
    updated: "2026-05-01",
  },
  {
    id: "VD 0004",
    name: "上海乐尔芙农业科技有限公司",
    status: "合作中",
    contact: "程琳",
    phone: "13901621696",
    cert: "已过期",
    buyer: "张伟",
    updated: "2026-04-28",
  },
  {
    id: "VD 0006",
    name: "南京云杉办公用品有限公司",
    status: "合作中",
    contact: "吴倩",
    phone: "13700008888",
    cert: "正常",
    buyer: "李娜",
    updated: "2026-04-18",
  },
  {
    id: "VD 0007",
    name: "上海办公伙伴有限公司",
    status: "合作中",
    contact: "许雯",
    phone: "13600007777",
    cert: "正常",
    buyer: "张伟",
    updated: "2026-05-08",
  },
];

const prs = [
  ["PRG-202605-001", "多部门", "办公与活动物料采购合集", "3张PR", "¥204,000", "张伟", "2026-06-12", "询价中", "未创建", "PR合集"],
  ["PRG-202605-002", "行政部", "办公室低值易耗品合集", "2张PR", "¥38,000", "张伟", "2026-06-18", "待询价", "未创建", "PR合集"],
  ["PR-202605-026", "市场部", "新品发布会物料与搭建服务", "1500", "¥78,000", "王敏", "2026-07-18", "审批中", "未创建"],
  ["PR-202605-027", "市场部", "活动现场摄影摄像服务", "1", "¥12,000", "王敏", "2026-07-20", "待询价", "未创建"],
  ["PR-202605-001", "市场部", "2026 年品牌活动礼品采购", "3000", "¥150,000", "王敏", "2026-05-30", "询价中", "未创建"],
  ["PR-202605-002", "IT部", "客服系统短信通道服务", "1", "¥280,000", "陈磊", "2026-06-10", "询价中", "未创建"],
  ["PR-202605-003", "行政部", "上海办公室绿植租摆服务", "12", "¥60,000", "刘珊", "2026-05-25", "待核价", "未创建"],
  ["PR-202605-004", "人力部", "年度培训场地服务", "1", "¥96,000", "周婷", "2026-06-20", "待核价", "未创建"],
  ["PR-202605-005", "财务部", "审计辅助服务", "1", "¥180,000", "孙浩", "2026-06-30", "待核价", "未创建"],
  ["PR-202605-006", "销售部", "客户答谢会物料", "800", "¥88,000", "何雨", "2026-06-05", "核价审批中", "未创建"],
  ["PR-202605-007", "品牌部", "线下活动摄影服务", "3", "¥36,000", "王敏", "2026-05-28", "核价退回", "未创建"],
  ["PR-202605-008", "运营部", "仓储标签打印设备", "6", "¥72,000", "林杰", "2026-06-12", "待建合同", "待建合同"],
  ["PR-202605-009", "法务部", "合同归档服务", "1", "¥45,000", "唐欣", "2026-07-01", "待建合同", "合同处理中"],
  ["PR-202605-010", "行政部", "办公室保洁外包", "12", "¥120,000", "刘珊", "2026-06-01", "待建合同", "合同处理中"],
  ["PR-202605-011", "市场部", "活动直播服务", "1", "¥90,000", "王敏", "2026-06-15", "待建合同", "合同处理中"],
  ["PR-202605-012", "IT部", "云监控订阅服务", "1", "¥64,000", "陈磊", "2026-06-18", "待建合同", "合同处理中"],
  ["PR-202605-013", "行政部", "会议室设备采购", "5", "¥85,000", "刘珊", "2026-06-25", "采购流程完成", "合同完成"],
  ["PR-202605-014", "客服部", "客服耳机采购", "40", "¥32,000", "赵阳", "2026-06-26", "采购流程完成", "合同完成"],
  ["PR-202605-015", "财务部", "税务咨询服务", "1", "¥55,000", "孙浩", "2026-06-28", "采购流程完成", "合同完成"],
  ["PR-202605-016", "运营部", "仓库打包耗材", "1000", "¥48,000", "林杰", "2026-06-30", "采购流程完成", "合同完成"],
  ["PR-202605-017", "销售部", "客户沙龙礼品", "500", "¥66,000", "何雨", "2026-07-05", "采购流程完成", "合同完成"],
  ["PR-202605-018", "品牌部", "品牌视觉设计服务", "1", "¥110,000", "王敏", "2026-07-08", "采购流程完成", "合同完成"],
  ["PR-202605-019", "行政部", "办公室保洁外包续签", "12", "¥120,000", "刘珊", "2026-07-10", "采购流程完成", "合同完成"],
  ["PR-202605-020", "人力部", "候选人测评服务", "1", "¥38,000", "周婷", "2026-07-12", "采购流程完成", "合同完成"],
];

const prBundleItems = {
  "PRG-202605-001": [
    ["PR-202605-021", "行政部", "办公文具补充采购", "¥42,000", "刘珊", "2026-06-08", "已合并"],
    ["PR-202605-022", "市场部", "线下活动易耗品", "¥86,000", "王敏", "2026-06-10", "已合并"],
    ["PR-202605-023", "运营部", "仓库包装辅助物料", "¥76,000", "林杰", "2026-06-12", "已合并"],
  ],
  "PRG-202605-002": [
    ["PR-202605-024", "行政部", "办公区清洁用品", "¥18,000", "刘珊", "2026-06-15", "已合并"],
    ["PR-202605-025", "行政部", "茶水间补充物资", "¥20,000", "刘珊", "2026-06-18", "已合并"],
  ],
};

const rfqs = [
  ["RFQ-202605-021", "办公与活动物料采购合集询价", "PRG-202605-001", "询比价", "供应商在线报价", "报价中", "第1轮", "4"],
  ["RFQ-202605-001", "2026 年品牌活动礼品采购", "PR-202605-001", "询比价", "供应商在线报价", "报价中", "第1轮", "3"],
  ["RFQ-202605-002", "短信通道服务采购", "PR-202605-002", "单一来源", "采购员代录报价", "草稿", "第1轮", "1"],
  ["RFQ-202605-003", "办公室绿植租摆服务", "PR-202605-003", "定向采购", "采购员代录报价", "核价中", "第1轮", "1"],
  ["RFQ-202605-004", "年度培训场地服务", "PR-202605-004", "询比价", "供应商在线报价", "报价截止", "第1轮", "4"],
  ["RFQ-202605-005", "审计辅助服务", "PR-202605-005", "单一来源", "采购员代录报价", "已开标", "第1轮", "1"],
  ["RFQ-202605-006", "客户答谢会物料", "PR-202605-006", "询比价", "供应商在线报价", "审批中", "第2轮", "3"],
  ["RFQ-202605-007", "活动摄影服务", "PR-202605-007", "询比价", "供应商在线报价", "已驳回", "第1轮", "3"],
  ["RFQ-202605-008", "仓储标签打印设备", "PR-202605-008", "定向采购", "采购员代录报价", "已完成", "第1轮", "1"],
  ["RFQ-202605-009", "合同归档服务", "PR-202605-009", "询比价", "供应商在线报价", "已完成", "第1轮", "3"],
  ["RFQ-202605-010", "办公室保洁外包", "PR-202605-010", "询比价", "供应商在线报价", "已完成", "第1轮", "3"],
  ["RFQ-202605-011", "活动直播服务", "PR-202605-011", "询比价", "供应商在线报价", "已完成", "第1轮", "3"],
  ["RFQ-202605-012", "云监控订阅服务", "PR-202605-012", "续约", "采购员代录报价", "已完成", "第1轮", "1"],
  ["RFQ-202605-013", "会议室设备采购", "PR-202605-013", "询比价", "供应商在线报价", "已完成", "第1轮", "3"],
  ["RFQ-202605-014", "客服耳机采购", "PR-202605-014", "询比价", "供应商在线报价", "已完成", "第1轮", "2"],
  ["RFQ-202605-015", "税务咨询服务", "PR-202605-015", "单一来源", "采购员代录报价", "已完成", "第1轮", "1"],
  ["RFQ-202605-016", "仓库打包耗材", "PR-202605-016", "定向采购", "采购员代录报价", "已完成", "第1轮", "1"],
  ["RFQ-202605-017", "客户沙龙礼品", "PR-202605-017", "询比价", "供应商在线报价", "已完成", "第2轮", "3"],
  ["RFQ-202605-018", "品牌视觉设计服务", "PR-202605-018", "询比价", "供应商在线报价", "已完成", "第1轮", "3"],
  ["RFQ-202605-019", "办公室保洁外包续签", "PR-202605-019", "续约", "采购员代录报价", "已完成", "第1轮", "1"],
  ["RFQ-202605-020", "候选人测评服务", "PR-202605-020", "单一来源", "采购员代录报价", "已完成", "第1轮", "1"],
];

const contracts = [
  ["-", "仓储标签打印设备合同", "PR-202605-008", "RFQ-202605-008", "南京云杉办公用品有限公司", "¥72,000", "待建合同", "待建合同"],
  ["CON-202605-001", "合同归档服务合同", "PR-202605-009", "RFQ-202605-009", "上海办公伙伴有限公司", "¥45,000", "合同草稿", "合同处理中"],
  ["CON-202605-002", "办公室保洁外包合同", "PR-202605-010", "RFQ-202605-010", "上海办公伙伴有限公司", "¥120,000", "外部审批推送中", "合同处理中"],
  ["CON-202605-003", "活动直播服务合同", "PR-202605-011", "RFQ-202605-011", "北京远景会务服务有限公司", "¥90,000", "Worklife BPM审批中", "合同处理中"],
  ["CON-202605-004", "云监控订阅服务合同", "PR-202605-012", "RFQ-202605-012", "上海博夷信息技术有限公司", "¥64,000", "待签署", "合同处理中"],
  ["CON-202605-005", "会议室设备采购合同", "PR-202605-013", "RFQ-202605-013", "南京云杉办公用品有限公司", "¥85,000", "电子签署中", "合同处理中"],
  ["CON-202605-006", "客服耳机采购合同", "PR-202605-014", "RFQ-202605-014", "上海办公伙伴有限公司", "¥32,000", "合同完成", "合同完成"],
  ["CON-202605-007", "税务咨询服务合同", "PR-202605-015", "RFQ-202605-015", "上海博夷信息技术有限公司", "¥55,000", "合同完成", "合同完成"],
  ["CON-202605-008", "仓库打包耗材合同", "PR-202605-016", "RFQ-202605-016", "山西德利仁信息技术服务有限公司", "¥48,000", "合同完成", "合同完成"],
  ["CON-202605-009", "客户沙龙礼品合同", "PR-202605-017", "RFQ-202605-017", "山西德利仁信息技术服务有限公司", "¥66,000", "合同完成", "合同完成"],
  ["CON-202605-010", "品牌视觉设计服务合同", "PR-202605-018", "RFQ-202605-018", "北京远景会务服务有限公司", "¥110,000", "合同完成", "合同完成"],
  ["CON-202605-011", "办公室保洁外包续签合同", "PR-202605-019", "RFQ-202605-019", "上海办公伙伴有限公司", "¥120,000", "合同完成", "合同完成"],
  ["CON-202605-012", "候选人测评服务合同", "PR-202605-020", "RFQ-202605-020", "上海博夷信息技术有限公司", "¥38,000", "合同完成", "合同完成"],
];

const supplierRfqRows = [
  ["RFQ-202605-001", "2026 年品牌活动礼品采购", "CDP集团", "2026-05-12 18:00", "报价中", "待报价"],
  ["RFQ-202605-002", "短信通道服务采购", "CDP集团", "2026-05-13 18:00", "报价中", "已报价"],
  ["RFQ-202605-003", "办公室绿植租摆服务", "CDP集团", "2026-05-10 18:00", "报价中", "已退回"],
  ["RFQ-202605-013", "会议室设备采购", "CDP集团", "2026-05-20 18:00", "已完成", "已完成"],
];

const poRows = [
  ["PO-202605-001", "PR-202605-014", "CON-202605-006", "RFQ-202605-014", "上海办公伙伴有限公司", "¥32,000", "已生成", "未付款", "PO已生成，待付款处理"],
  ["PO-202605-002", "PR-202605-015", "CON-202605-007", "RFQ-202605-015", "上海博夷信息技术有限公司", "¥55,000", "同步中", "未付款", "推送台账中"],
  ["PO-202605-003", "PR-202605-016", "CON-202605-008", "RFQ-202605-016", "山西德利仁信息技术服务有限公司", "¥48,000", "同步失败", "未付款", "供应商税号缺失"],
  ["PO-202605-004", "PR-202605-017", "CON-202605-009", "RFQ-202605-017", "山西德利仁信息技术服务有限公司", "¥66,000", "已同步", "待付款", "台账同步成功，待财务付款"],
  ["PO-202605-005", "PR-202605-018", "CON-202605-010", "RFQ-202605-018", "北京远景会务服务有限公司", "¥110,000", "已同步", "付款中", "财务已选择PO"],
  ["PO-202605-006", "PR-202605-019", "CON-202605-011", "RFQ-202605-019", "上海办公伙伴有限公司", "¥120,000", "已同步", "部分付款", "已付 ¥60,000"],
  ["PO-202605-007", "PR-202605-013", "CON-202605-005", "RFQ-202605-013", "南京云杉办公用品有限公司", "¥85,000", "已同步", "付款完成", "台账已回传 PAY-202605-007"],
  ["PO-202605-008", "PR-202605-020", "CON-202605-012", "RFQ-202605-020", "上海博夷信息技术有限公司", "¥38,000", "已同步", "付款失败", "台账回传失败：收款账户异常"],
  ["PO-202605-009", "PR-202605-014", "CON-202605-006", "RFQ-202605-014", "上海办公伙伴有限公司", "¥0", "已取消", "未付款", "重复创建后取消，保留审计记录"],
];

const stateOptions = {
  supplier: ["创建成功", "待进入", "待完善信息", "待审核信息", "合作中", "已停用"],
  certExpiry: ["正常", "即将到期", "已过期", "待上传"],
  reviewType: ["信息变更", "证件审核"],
  reviewStatus: ["待审核信息", "已通过", "已驳回"],
  pr: ["草稿", "审批中", "审批驳回", "待询价", "询价中", "待核价", "核价审批中", "核价退回", "待建合同", "采购流程完成", "已取消"],
  prType: ["普通PR", "PR合集"],
  contract: ["未创建", "合同草稿", "外部审批推送中", "Worklife BPM审批中", "Worklife BPM审批通过", "待签署", "电子签署中", "合同完成", "已取消"],
  poApproval: ["草稿", "审批中", "审批通过", "审批驳回", "已取消"],
  ledger: ["未生成", "待同步", "同步中", "已同步", "同步失败", "已取消"],
  po: ["未生成", "待同步", "同步中", "已同步", "同步失败", "已取消"],
  payment: ["未付款", "待付款", "付款中", "部分付款", "付款完成", "付款失败"],
  rfq: ["草稿", "报价中", "报价截止", "已开标", "核价中", "审批中", "已完成", "已驳回", "已取消"],
  strategy: ["询比价", "单一来源", "定向采购", "续约"],
  quoteMode: ["供应商在线报价", "采购员代录报价"],
  quote: ["待报价", "已报价", "已退回", "已完成"],
  email: ["成功", "失败", "待发送"],
};

const quoteRows = [
  ["定制帆布袋", "3000", "个", "¥18.00", "¥54,000", "15天"],
  ["礼盒包装服务", "3000", "套", "¥4.50", "¥13,500", "15天"],
];

const winningSuppliers = [
  ["山西德利仁信息技术服务有限公司", "定制帆布袋", "3000", "¥17.50", "¥52,500"],
  ["上海博夷信息技术有限公司", "礼盒包装服务", "3000", "¥4.50", "¥13,500"],
];

function tag(text, type) {
  const map = {
    合作中: "green",
    正常: "green",
    已报价: "green",
    已完成: "green",
    已通过: "green",
    合同完成: "green",
    采购流程完成: "green",
    已生成: "blue",
    审批中: "orange",
    审批通过: "green",
    审批驳回: "red",
    待同步: "orange",
    已同步: "green",
    同步中: "orange",
    同步失败: "red",
    未生成: "gray",
    未付款: "gray",
    待付款: "orange",
    付款中: "orange",
    部分付款: "blue",
    付款完成: "green",
    付款失败: "red",
    创建成功: "blue",
    待进入: "gray",
    启用: "green",
    成功: "green",
    合同处理中: "blue",
    "Worklife BPM审批通过": "green",
    已开标: "purple",
    待核价: "purple",
    待建合同: "purple",
    已关联询价: "blue",
    询价中: "blue",
    核价审批中: "orange",
    核价退回: "red",
    待审核信息: "blue",
    报价中: "blue",
    报价截止: "purple",
    核价中: "blue",
    合同草稿: "blue",
    待签署: "blue",
    待上传: "orange",
    待审批: "orange",
    未创建: "orange",
    待处理: "orange",
    待报价: "orange",
    即将到期: "orange",
    审批中: "orange",
    "Worklife BPM审批中": "orange",
    外部审批推送中: "orange",
    审批驳回: "red",
    电子签署中: "orange",
    待签署: "orange",
    待完善信息: "orange",
    已过期: "red",
    已退回: "red",
    已驳回: "red",
    已取消: "red",
    草稿: "gray",
    已关联: "gray",
    采购员代录报价: "purple",
    供应商在线报价: "blue",
    续约: "purple",
    定向采购: "orange",
  };
  return `<span class="tag ${type || map[text] || "gray"}">${text}</span>`;
}

function money(value) {
  return `<strong>${value}</strong>`;
}

function contractAmount(value) {
  if (!value) return "-";
  const raw = String(value).trim();
  return raw.replace(/^(¥|￥|USD|EUR|CNY)\s*/i, "").trim();
}

function contractCurrency(value) {
  if (!value) return "CNY";
  const raw = String(value).trim().toUpperCase();
  if (raw.startsWith("USD")) return "USD";
  if (raw.startsWith("EUR")) return "EUR";
  if (raw.startsWith("CNY")) return "CNY";
  if (raw.startsWith("¥") || raw.startsWith("￥")) return "CNY";
  return "CNY";
}

function prContractStatus(prNo, fallback = "未创建") {
  if (prNo !== "PR-202605-001") return fallback;
  if (state.contractStatus === "合同完成") return "合同完成";
  if (state.contractStatus === "未创建" && state.rfqStatus === "已完成") return "待建合同";
  if (state.contractStatus !== "未创建") return "合同处理中";
  return fallback;
}

function prProcessStatus(pr) {
  if (!pr) return "待询价";
  const status = pr[7];
  if (pr[0] === "PR-202605-001" && state.contractStatus === "合同完成") return "采购流程完成";
  if (pr[0] === "PR-202605-001" && state.rfqStatus === "已完成") return "待建合同";
  const map = {
    待处理: "待询价",
    已关联询价: "询价中",
    合同处理中: "待建合同",
    合同完成: "采购流程完成",
    已关联询价: "询价中",
    待处理: "待询价",
  };
  return map[status] || status;
}

function prPoStatus(prNo, fallback = "未生成") {
  if (prNo !== "PR-202605-001") {
    const row = poRows.find((po) => po[1] === prNo);
    return row ? row[6] : fallback;
  }
  if (state.contractStatus !== "合同完成") return "未生成";
  return state.poStatus;
}

function prPaymentStatus(prNo, fallback = "未付款") {
  if (prNo !== "PR-202605-001") {
    const row = poRows.find((po) => po[1] === prNo);
    return row ? row[7] : fallback;
  }
  if (state.contractStatus !== "合同完成") return "未付款";
  return state.paymentStatus;
}

function parseCny(value) {
  return Number(String(value || "0").replace(/[¥,]/g, "")) || 0;
}

function formatCny(value) {
  return `¥${Number(value || 0).toLocaleString("zh-CN")}`;
}

function paidAmountForPo(po) {
  if (!po) return 0;
  if (po[7] === "付款完成") return parseCny(po[5]);
  if (po[7] === "部分付款") return 60000;
  return 0;
}

function poApprovalStatus(po, isDynamic = false) {
  if (isDynamic) return state.poApprovalStatus || (state.poStatus === "未生成" ? "草稿" : "审批通过");
  if (!po) return "草稿";
  if (po[6] === "已取消") return "审批驳回";
  return "审批通过";
}

function poLedgerStatus(po, isDynamic = false) {
  const status = isDynamic ? state.poStatus : po?.[6];
  if (!status || status === "未生成") return "未生成";
  if (["已生成", "审批通过"].includes(status)) return "待同步";
  return status;
}

function poCurrency(value) {
  return contractCurrency(value);
}

function relatedPoRowsForPr(prNo) {
  const dynamicPrimaryPo = prNo === "PR-202605-001" && state.contractStatus === "合同完成"
    ? [["PO-202605-010", "PR-202605-001", "CON-202605-013", "RFQ-202605-001", "山西德利仁、上海博夷", "¥70,914", state.poStatus, state.paymentStatus, "演示流：合同归档后发起PO"]]
    : [];
  return [...dynamicPrimaryPo, ...poRows.filter((po) => po[1] === prNo)];
}

function paidTotalForPr(prNo) {
  return formatCny(relatedPoRowsForPr(prNo).reduce((sum, po) => sum + paidAmountForPo(po), 0));
}

function findPr(prNo = state.selectedPr) {
  return prs.find((p) => p[0] === prNo) || prs[0];
}

function findRfq(rfqNo = state.selectedRfq) {
  return rfqs.find((r) => r[0] === rfqNo) || rfqs.find((r) => r[2] === state.selectedPr) || rfqs[0];
}

function findContract(contractNo = state.selectedContract) {
  if (contractNo) return contracts.find((c) => c[0] === contractNo) || contracts.find((c) => c[2] === state.selectedPr);
  return contracts.find((c) => c[2] === state.selectedPr) || null;
}

function findPo(poNo = state.selectedPo) {
  if (poNo) return poRows.find((po) => po[0] === poNo) || poRows.find((po) => po[1] === state.selectedPr);
  return poRows.find((po) => po[1] === state.selectedPr) || null;
}

function prType(pr) {
  return pr && pr[9] === "PR合集" ? "PR合集" : "普通PR";
}

function isPrBundle(pr) {
  return prType(pr) === "PR合集";
}

function hasGeneratedRfq(prNo) {
  return rfqs.some((r) => r[2] === prNo);
}

function selectPr(prNo, target = "pr-detail") {
  const pr = findPr(prNo);
  const rfq = rfqs.find((r) => r[2] === pr[0]);
  const contract = contracts.find((c) => c[2] === pr[0]);
  const po = poRows.find((p) => p[1] === pr[0]);
  state.selectedPr = pr[0];
  state.selectedRfq = rfq ? rfq[0] : null;
  state.selectedContract = contract ? contract[0] : null;
  state.selectedPo = po ? po[0] : null;
  if (contract) state.contractStatus = state.contractStatusByNo[contract[0]] || contract[6];
  state.prTab = "overview";
  setPage(target);
}

function selectRfq(rfqNo, target = "rfq-detail") {
  const rfq = findRfq(rfqNo);
  const contract = contracts.find((c) => c[3] === rfq[0]);
  const po = poRows.find((p) => p[3] === rfq[0]);
  state.selectedRfq = rfq[0];
  state.selectedPr = rfq[2];
  state.selectedContract = contract ? contract[0] : null;
  state.selectedPo = po ? po[0] : null;
  if (contract) state.contractStatus = state.contractStatusByNo[contract[0]] || contract[6];
  setPage(target);
}

function selectContract(contractNo, prNo, target = "contract-detail") {
  const contract = contracts.find((c) => c[0] === contractNo && c[2] === prNo) || contracts.find((c) => c[2] === prNo);
  if (contract) {
    state.selectedContract = contract[0];
    state.selectedPr = contract[2];
    state.selectedRfq = contract[3];
    const po = poRows.find((p) => p[2] === contract[0] || p[1] === contract[2]);
    state.selectedPo = po ? po[0] : null;
    state.contractStatus = state.contractStatusByNo[contract[0]] || contract[6];
  }
  setPage(target);
}

function selectPo(poNo, target = "po-detail") {
  const po = findPo(poNo);
  if (po) {
    state.selectedPo = po[0];
    state.selectedPr = po[1];
    state.selectedContract = po[2];
    state.selectedRfq = po[3];
    const contract = contracts.find((c) => c[0] === po[2]);
    if (contract) state.contractStatus = state.contractStatusByNo[contract[0]] || contract[6];
  }
  setPage(target);
}

function setContractStatus(status) {
  state.contractStatus = status;
  if (state.selectedContract) state.contractStatusByNo[state.selectedContract] = status;
  if (status === "合同完成") {
    state.poStatus = "未生成";
    state.paymentStatus = "未付款";
  }
  setPage("contract-detail");
}

function selectFieldLibrary(key) {
  state.selectedFieldLibrary = fieldLibraries[key] ? key : "certificate";
  setPage("admin-field-library-detail");
}

function selectFormTemplate(key) {
  state.selectedFormTemplate = fieldLibraries[key] ? key : "certificate";
  state.selectedFieldLibrary = state.selectedFormTemplate;
  setPage("admin-form-detail");
}

function groupedFieldSummary(fields) {
  const groups = {};
  fields.forEach((item) => {
    const group = item[5] || "未分组";
    if (!groups[group]) groups[group] = [];
    groups[group].push(item[1]);
  });
  return Object.entries(groups).map(([group, names]) => [
    group,
    names.slice(0, 4).join("、"),
    group.includes("财务") || group.includes("预算") || group.includes("银行") ? "用于成本归集、付款和财务校验" :
      group.includes("联系方式") || group.includes("通知") ? "用于邮件通知、RFQ邀请和业务沟通" :
      group.includes("证件") || group.includes("有效期") || group.includes("许可证") ? "用于资质审核、动态字段和到期提醒" :
      "用于表单填写、列表展示和审核追溯",
  ]);
}

function inferRoleForPage(page, currentRole = state.role) {
  if (page.startsWith("business")) return "business";
  if (page.startsWith("admin")) return "admin";
  if (currentRole === "business" && ["pr-detail", "po-detail", "contract-detail"].includes(page)) return "business";
  const buyerPages = [
    "suppliers",
    "supplier-detail",
    "supplier-create",
    "supplier-edit",
    "buyer-cert-upload",
    "reviews",
    "review-info-detail",
    "review-cert-detail",
    "purchase-requests",
    "pr-detail",
    "rfqs",
    "create-rfq",
    "rfq-edit",
    "rfq-detail",
    "quote-detail",
    "proxy-quote",
    "new-round",
    "bid-summary",
    "price-review",
    "price-review-quotes",
    "approval-status",
    "contracts",
    "contract-create",
    "contract-detail",
    "pos",
    "po-detail",
  ];
  if (page === "supplier-contact-edit") return currentRole === "supplier" ? "supplier" : currentRole === "admin" ? "admin" : "buyer";
  if (buyerPages.includes(page)) {
    if (currentRole === "admin") return "admin";
    return "buyer";
  }
  if (page.startsWith("supplier")) return "supplier";
  return currentRole;
}

function routeHash() {
  return `#/${state.role}/${state.page}`;
}

function syncHash(replace = false) {
  if (typeof window === "undefined" || !window.location) return;
  const next = routeHash();
  if (window.location.hash === next) return;
  suppressHashSync = true;
  if (replace && window.history && window.history.replaceState) {
    window.history.replaceState(null, "", next);
  } else {
    window.location.hash = next;
  }
  suppressHashSync = false;
}

function applyHashRoute() {
  if (typeof window === "undefined" || !window.location) return false;
  const parts = window.location.hash.replace(/^#\/?/, "").split("/").filter(Boolean);
  if (parts.length < 2 || !roleMeta[parts[0]]) return false;
  state.authed = true;
  state.role = parts[0];
  state.page = parts[1];
  state.pageHistory = [];
  render();
  return true;
}

function setPage(page, options = {}) {
  if (state.authed && options.track !== false && state.page !== page) {
    state.pageHistory.push({ role: state.role, page: state.page });
    if (state.pageHistory.length > 30) state.pageHistory.shift();
  }
  state.page = page;
  state.role = inferRoleForPage(page);
  render();
  if (options.hash !== false) syncHash(Boolean(options.replace));
}

function setRole(role, options = {}) {
  state.role = role;
  state.page = roleHomePage(role);
  state.reviewTab = "todo";
  state.pageHistory = [];
  render();
  if (options.hash !== false) syncHash(Boolean(options.replace));
}

function openModal(type) {
  state.modal = type;
  render();
}

function closeModal() {
  state.modal = null;
  render();
}

function confirmModal() {
  const titleMap = {
    notifications: "消息已查看",
    businessBudgetLine: "预算明细行已保存",
    businessPoLine: "PO明细行已保存",
    priceAdjust: "改价记录已保存",
    awardLine: "最终中标明细行已添加",
    priceReviewConfirm: "核价结果已提交采购经理审批",
    offlineQuote: "代录供应商报价已保存",
    offlineArchive: "线下签署文件已上传，合同已归档",
    mergePrBundle: "PR合集已创建",
    removeBundlePr: "已移除合集明细",
    quoteSubmitted: "报价已提交",
  };
  const message = titleMap[state.modal] || "操作已确认";
  if (state.modal === "priceReviewConfirm") {
    state.rfqStatus = "审批中";
    state.modal = null;
    setPage("approval-status");
    showToast(message);
    return;
  }
  if (state.modal === "offlineArchive") {
    state.contractStatus = "合同完成";
    if (state.selectedContract) state.contractStatusByNo[state.selectedContract] = "合同完成";
    state.poStatus = "未生成";
    state.paymentStatus = "未付款";
  }
  state.modal = null;
  showToast(message);
}

function showToast(message) {
  state.toast = message;
  render();
  if (typeof window !== "undefined") {
    window.clearTimeout(window.__procurementToastTimer);
    window.__procurementToastTimer = window.setTimeout(() => {
      state.toast = null;
      render();
    }, 2200);
  }
}

function goBack() {
  const last = state.pageHistory.pop();
  if (last) {
    state.role = last.role;
    state.page = last.page;
    render();
    syncHash(true);
    return;
  }
  setRole(state.role, { replace: true });
}

function login(role = "buyer") {
  state.authed = true;
  setRole(role);
}

function logout() {
  state.authed = false;
  state.pageHistory = [];
  if (typeof window !== "undefined" && window.history && window.location) {
    window.history.pushState(null, "", window.location.pathname);
  }
  render();
}

function isNavActive(page) {
  if (page === "admin-report-executive") return state.page.startsWith("admin-report-");
  return state.page === page;
}

function roleHomePage(role = state.role) {
  return role === "admin" ? "admin-dashboard" : role === "supplier" ? "supplier-company" : role === "business" ? "business-prs" : "buyer-dashboard";
}

function rolePortalLabel(role = state.role) {
  if (role === "admin") return "采购经理端";
  if (role === "buyer") return "采购员端";
  if (role === "business") return "业务人员端";
  return "供应商端";
}

function breadcrumbParents() {
  const supplierContactParent = state.role === "supplier" ? "supplier-contacts" : "supplier-detail";
  const reviewParent = state.role === "admin" ? "admin-approval" : "reviews";
  const rfqParent = state.role === "admin" ? "admin-rfqs" : "rfqs";
  const contractParent = state.role === "admin" ? "admin-contracts" : state.role === "business" ? "business-contracts" : "contracts";
  const poParent = state.role === "admin" ? "admin-pos" : state.role === "business" ? "business-pos" : "pos";
  const map = {
    suppliers: [],
    "supplier-detail": [["suppliers", "供应商管理"]],
    "supplier-create": [["suppliers", "供应商管理"]],
    "supplier-edit": [["suppliers", "供应商管理"], ["supplier-detail", "供应商详情"]],
    "buyer-cert-upload": [["suppliers", "供应商管理"], ["supplier-detail", "供应商详情"]],
    reviews: [],
    "review-info-detail": [[reviewParent, "审批中心"]],
    "review-cert-detail": [[reviewParent, "审批中心"]],
    "purchase-requests": [],
    "pr-detail": [["purchase-requests", "采购申请单"]],
    rfqs: [],
    "create-rfq": [[rfqParent, "询价单管理"]],
    "offline-sourcing": [[rfqParent, "询价单管理"]],
    "rfq-edit": [[rfqParent, "询价单管理"], ["rfq-detail", "询价单详情"]],
    "rfq-detail": [[rfqParent, "询价单管理"]],
    "quote-detail": [[rfqParent, "询价单管理"], ["rfq-detail", "询价单详情"]],
    "proxy-quote": [[rfqParent, "询价单管理"], ["rfq-detail", "询价单详情"]],
    "new-round": [[rfqParent, "询价单管理"], ["bid-summary", "开标汇总"]],
    "bid-summary": [[rfqParent, "询价单管理"], ["rfq-detail", "询价单详情"]],
    "price-review": [[rfqParent, "询价单管理"], ["rfq-detail", "询价单详情"]],
    "price-review-quotes": [[rfqParent, "询价单管理"], ["rfq-detail", "询价单详情"], ["price-review", "核价页面"]],
    "approval-status": [[rfqParent, "询价单管理"], ["rfq-detail", "询价单详情"]],
    contracts: [],
    "contract-create": [[contractParent, "合同管理"]],
    "contract-detail": [[contractParent, "合同管理"]],
    "admin-contract-detail": [["admin-contracts", "合同管理"]],
    pos: [],
    "admin-pos": [],
    "po-detail": [[poParent, "履约与付款"]],
    "business-prs": [],
    "business-pr-create": [["business-prs", "我的采购申请"]],
    "business-pr-detail": [["business-prs", "我的采购申请"]],
    "business-contracts": [],
    "business-pos": [],
    "business-po-create": [["business-pos", "我的PO"]],
    "admin-approval": [["admin-dashboard", "经理工作台"]],
    "admin-approval-detail": [["admin-approval", "审批中心"]],
    "admin-rfqs": [],
    "admin-contracts": [],
    "admin-report-spend": [["admin-report-executive", "采购分析"]],
    "admin-report-process": [["admin-report-executive", "采购分析"]],
    "admin-report-suppliers": [["admin-report-executive", "采购分析"]],
    "admin-report-risks": [["admin-report-executive", "采购分析"]],
    "admin-data-settings": [],
    "admin-buyers": [["admin-data-settings", "数据设置"]],
    "admin-field-settings": [["admin-data-settings", "数据设置"]],
    "admin-field-library-detail": [["admin-data-settings", "数据设置"], ["admin-field-settings", "字段设置"]],
    "admin-field-create": [["admin-data-settings", "数据设置"], ["admin-field-settings", "字段设置"], ["admin-field-library-detail", "字段库详情"]],
    "admin-field-detail": [["admin-data-settings", "数据设置"], ["admin-field-settings", "字段设置"], ["admin-field-library-detail", "字段库详情"]],
    "admin-form-settings": [["admin-data-settings", "数据设置"]],
    "admin-form-detail": [["admin-data-settings", "数据设置"], ["admin-form-settings", "表单设置"]],
    "admin-form-create": [["admin-data-settings", "数据设置"], ["admin-form-settings", "表单设置"]],
    "admin-form-config": [["admin-data-settings", "数据设置"], ["admin-form-settings", "表单设置"], ["admin-form-detail", "表单详情"]],
    "admin-form-preview": [["admin-data-settings", "数据设置"], ["admin-form-settings", "表单设置"], ["admin-form-detail", "表单详情"], ["admin-form-config", "配置表单字段"]],
    "supplier-company": [],
    "supplier-cert-upload": [["supplier-company", "企业信息"]],
    "supplier-contacts": [],
    "supplier-contact-edit": [[supplierContactParent, state.role === "supplier" ? "联系人管理" : "供应商详情"]],
    "supplier-rfqs": [],
    "supplier-rfq-detail": [["supplier-rfqs", "我的询价单"]],
    "supplier-quote": [["supplier-rfqs", "我的询价单"], ["supplier-rfq-detail", "询价单详情"]],
    "supplier-contracts": [],
    "supplier-contract-detail": [["supplier-contracts", "合同管理"]],
    "email-detail": [["email-logs", "邮件日志"]],
  };
  return map[state.page] || [];
}

function breadcrumbHtml() {
  const portalName = rolePortalLabel();
  const items = [
    ["首页", `setRole('${state.role}')`],
    [portalName, `setRole('${state.role}')`],
    ...breadcrumbParents().map(([page, label]) => [label, `setPage('${page}')`]),
    [currentTitle(), `setPage('${state.page}')`],
  ];
  return `<nav class="breadcrumb" aria-label="面包屑导航">${items.map(([label, action], index) => `
    ${index > 0 ? `<span class="breadcrumb-sep">/</span>` : ""}
    <button class="breadcrumb-btn ${index === items.length - 1 ? "current" : ""}" onclick="${action}">${label}</button>
  `).join("")}</nav>`;
}

function appShell(content) {
  const meta = roleMeta[state.role];
  const portalName = rolePortalLabel();
  return `
    <div class="layout">
      <aside class="sidebar">
        <div class="sidebar-brand">
          <div class="logo">采</div>
          <div><strong>采购管理平台</strong><span>Procurement SaaS</span></div>
        </div>
        <div class="role-card">
          <div class="role-card-title">${portalName}</div>
          <div class="role-card-desc">${meta.name} · ${meta.label}${meta.company ? " · " + meta.company : ""}</div>
        </div>
        <div>
          <div class="nav-section-title">功能导航</div>
          <nav class="nav">
            ${navs[state.role]
              .map(([page, icon, label]) => `<button class="nav-btn ${isNavActive(page) ? "active" : ""}" onclick="setPage('${page}')"><span class="nav-icon">${icon}</span>${label}</button>`)
              .join("")}
          </nav>
        </div>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="topbar-left">
            <button class="btn ghost back-btn" onclick="goBack()">‹ 返回</button>
            ${breadcrumbHtml()}
          </div>
          <div class="top-actions">
            <button class="btn ghost" onclick="openModal('notifications')">消息 8</button>
            <div class="user-pill">
              <div class="avatar">${meta.initial}</div>
              <div>
                <strong>${meta.name}</strong>
                <div class="hint">${meta.label}${meta.company ? " / " + meta.company : ""}</div>
              </div>
            </div>
            <button class="btn" onclick="logout()">退出到角色入口</button>
          </div>
        </header>
        <section class="content">${content}</section>
      </main>
    </div>
    ${modalHtml()}
    ${toastHtml()}
  `;
}

function currentTitle() {
  const all = Object.values(navs).flat();
  const match = all.find(([page]) => page === state.page);
  const custom = {
    "create-rfq": "创建询价单",
    "offline-sourcing": "代录报价",
    "rfq-detail": "询价单详情",
    "bid-summary": "开标汇总",
    "price-review": "核价页面",
    "price-review-quotes": "核价页面",
    "approval-status": "审批状态",
    "supplier-detail": "供应商详情",
    "supplier-create": "创建供应商",
    "supplier-edit": "编辑供应商",
    "buyer-cert-upload": "手动添加供应商证件",
    "review-info-detail": "信息变更审核详情",
    "review-cert-detail": "证件审核详情",
    "pr-detail": "采购申请单详情",
    "rfq-edit": "编辑询价单",
    "supplier-quote": "报价页面",
    "supplier-rfq-detail": "供应商询价单详情",
    "quote-detail": "报价详情",
    "proxy-quote": "代询价",
    "new-round": "开启新一轮",
    contracts: "合同管理",
    "contract-create": "创建合同",
    "contract-detail": "合同详情",
    pos: "履约与付款",
    "po-detail": "PO详情",
    "admin-pos": "履约与付款",
    "admin-approval-detail": "核价审批详情",
    "admin-report-executive": "采购分析",
    "admin-report-spend": "支出分析",
    "admin-report-process": "流程效率",
    "admin-report-suppliers": "供应商分析",
    "admin-report-risks": "异常与风险",
    "admin-contracts": "合同管理",
    "admin-data-settings": "数据设置",
    "admin-buyers": "采购员账号",
    "admin-contract-detail": "合同详情",
    "admin-field-settings": "字段设置",
    "admin-field-library-detail": "字段库详情",
    "admin-field-create": "创建字段",
    "admin-field-detail": "字段详情",
    "admin-form-settings": "表单设置",
    "admin-form-detail": "表单详情",
    "admin-form-create": "创建表单",
    "admin-form-config": "配置表单字段",
    "admin-form-preview": "表单预览",
    "business-prs": "我的采购申请",
    "business-pr-create": "创建采购申请单",
    "business-pr-detail": "采购申请单跟踪",
    "business-contracts": "我的合同",
    "business-pos": "我的PO",
    "business-po-create": "创建PO",
    "email-detail": "邮件详情",
    "supplier-cert-upload": "上传证件",
    "supplier-contact-edit": "编辑联系人",
    "supplier-contracts": "合同管理",
    "supplier-contract-detail": "合同详情",
  };
  return custom[state.page] || (match ? match[2] : "工作台");
}

function loginPage() {
  return `
    <div class="login-shell">
      <div class="login-panel">
        <div class="brand-mark">采</div>
        <h1>采购管理平台</h1>
        <p>请选择角色入口。四个端的菜单、工作台和可操作页面互相独立。</p>
        <div class="login-roles">
          <button class="login-role-card" onclick="login('business')">
            <span class="login-role-icon">业</span>
            <strong>业务人员端</strong>
            <em>采购申请单创建、审批跟踪、合同与PO进度查看</em>
          </button>
          <button class="login-role-card" onclick="login('buyer')">
            <span class="login-role-icon">采</span>
            <strong>采购员端</strong>
            <em>供应商主数据、PR、询价、核价、合同创建</em>
          </button>
          <button class="login-role-card" onclick="login('admin')">
            <span class="login-role-icon">经</span>
            <strong>采购经理端</strong>
            <em>核价审批、全量询价查询、合同管理和基础配置</em>
          </button>
          <button class="login-role-card" onclick="login('supplier')">
            <span class="login-role-icon">供</span>
            <strong>供应商端</strong>
            <em>企业信息、证件联系人、我的询价单和报价</em>
          </button>
        </div>
        <div class="notice" style="margin-top:16px;">静态原型入口：无需真实账号密码，点击角色卡片即可进入对应独立页面。</div>
      </div>
    </div>
  `;
}

function pageHead(title, desc, actions = "") {
  return `
    <div class="page-head">
      <div><h2>${title}</h2><p>${desc}</p></div>
      <div class="actions">${actions}</div>
    </div>
  `;
}

function metrics(items) {
  return `<div class="grid cols-${Math.min(items.length, 5)}">${items
    .map((x) => {
      const [label, value, desc, page] = x;
      const click = page ? ` role="button" tabindex="0" onclick="event.stopPropagation(); setPage('${page}')" onkeydown="if(event.key==='Enter'){event.stopPropagation(); setPage('${page}')}"` : "";
      return `<div class="metric ${page ? "clickable-card" : ""}"${click}><div class="label">${label}</div><div class="value">${value}</div>${desc ? `<div class="delta">${desc}</div>` : ""}</div>`;
    })
    .join("")}</div>`;
}

function businessOwnedPrs() {
  return prs.filter((p) => p[5] === roleMeta.business.name);
}

function assignedBuyerForPr(pr) {
  if (!pr) return "-";
  if (["草稿", "审批中", "审批驳回"].includes(pr[7])) return "待审批通过后分配";
  return pr[0] === "PR-202605-007" ? "李娜" : "张伟";
}

function businessPrsPage() {
  const f = currentFilters();
  const rows = businessOwnedPrs().filter((p) =>
    textIncludes([p[0], p[2]], f.f0) &&
    exactMatch(prProcessStatus(p), f.f1) &&
    exactMatch(prContractStatus(p[0], p[8]), f.f2) &&
    exactMatch(prPaymentStatus(p[0]), f.f3)
  );
  return `
    ${pageHead("我的采购申请", "业务人员在采购系统发起 PR，并跟踪 Worklife BPM 审批、采购员分配、合同、PO 和付款进展。", `
      <button class="btn primary" onclick="setPage('business-pr-create')">创建采购申请单</button>
    `)}
    <div class="notice" style="margin-bottom:14px;">业务人员端不展示 RFQ。PR状态只表示申请单自身和采购流程阶段；合同状态、PO状态、付款状态分别独立展示，不混入 PR 状态。</div>
    ${filters([
      "PR号 / 需求内容",
      { label: "PR状态（采购流程）", options: stateOptions.pr },
      { label: "合同状态", options: stateOptions.contract },
      { label: "付款状态", options: stateOptions.payment },
    ])}
    ${tableWrap(["PR号", "部门", "需求摘要", "预算", "希望完成时间", "PR状态", "分配采购员", "合同状态", "PO状态", "付款状态", "已付款总额", "操作"], rows.map((p) => {
      return [
        `<button class="btn link" onclick="selectPr('${p[0]}','business-pr-detail')">${p[0]}</button>`,
        p[1],
        p[2],
        p[4],
        p[6],
        tag(prProcessStatus(p)),
        assignedBuyerForPr(p),
        tag(prContractStatus(p[0], p[8])),
        tag(poApprovalStatus(relatedPoRowsForPr(p[0])[0])),
        tag(prPaymentStatus(p[0])),
        paidTotalForPr(p[0]),
        `<button class="btn link" onclick="selectPr('${p[0]}','business-pr-detail')">跟踪详情</button>`,
      ];
    }))}
  `;
}

function businessPrCreatePage() {
  const isSoleSupplier = state.prForm.isSoleSupplier === "是";
  const isBusinessCost = state.prForm.procurementType === "业务成本类采购";
  const supplierTypeBlock = isSoleSupplier ? `
        ${controlledSelectField("供应商类型", ["市场部活动相关供应商", "客户指定供应商", "已有设备指定供应商", "其他"], state.prForm.supplierType, "supplierType")}
      ` : "";
  const businessCostBlock = isBusinessCost ? `
    <div style="height:16px"></div>
    <div class="card conditional-card">
      <div class="card-title"><h3>业务成本类采购信息</h3><span class="hint">采购类型为业务成本类采购时必填</span></div>
      <div class="form-grid">
        ${field("客户名称 Customer Name", "CDP重点客户A")}
        ${controlledSelectField("CDP公司抬头 CDP Group Title", ["CDP集团", "CDP上海公司", "CDP北京公司"], state.prForm.cdpGroupTitle, "cdpGroupTitle")}
        ${field("上传双盖章销售合同", "销售合同_双方盖章版.pdf")}
        ${field("CRM合同编号/订单编号", "CRM-ORD-202605-8891")}
        ${controlledSelectField("该采购预算是否体现在项目利润表中", ["是", "否"], state.prForm.pnlIncluded, "pnlIncluded")}
      </div>
    </div>
  ` : "";
  return `
    ${pageHead("创建采购申请单", "业务人员填写采购需求和预算明细，提交后进入 Worklife BPM 审批系统；审批通过后回传 PR 编号并分配采购员。")}
    ${summary("PR-202605-026 · 新品发布会物料与搭建服务", `${tag("草稿")} ${tag("待提交审批")}`, "演示数据：王敏 / 市场部 / 预算 ¥78,000")}
    <div class="card">
      <div class="card-title"><h3>申请基础信息</h3><span class="hint">隐藏字段不参与提交和校验</span></div>
      <div class="form-grid">
        ${field("PR主题", "新品发布会物料与搭建服务")}
        ${field("申请日期", "2026-05-26")}
        ${field("PR编号", "提交审批后生成", true)}
        ${field("申请人", roleMeta.business.name, true)}
        ${field("申请部门", roleMeta.business.department, true)}
        ${field("Enduser", "王敏", true)}
        ${field("Department Head", "赵明", true)}
        ${field("成本中心 Cost Center", "MKT-2026-BRAND")}
        ${controlledSelectField("采购类型 Type", ["公司费用类采购", "业务成本类采购"], state.prForm.procurementType, "procurementType")}
        ${controlledSelectField("是否唯一供应商", ["否", "是"], state.prForm.isSoleSupplier, "isSoleSupplier")}
        ${supplierTypeBlock}
        ${textareaField("产品/服务需求", "新品发布会需要定制伴手礼、活动搭建物料和现场执行服务。")}
        ${field("附件", "活动方案.pdf / 设计参考图.zip")}
      </div>
      <label class="check-line" style="margin-top:12px;"><input type="checkbox" checked /> 确认采购不涉及社保挂靠或代缴</label>
    </div>
    ${businessCostBlock}
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>预估采购金额 Budget</h3><button class="btn" onclick="openModal('businessBudgetLine')">添加行</button></div>
      ${table(["序号", "产品/服务需求", "采购数量", "单位", "采购单价", "货币", "总价", "备注", "操作"], [
        ["1", "定制伴手礼套装", "800", "套", "65", "CNY", "¥52,000", "含包装设计", `<button class="btn link" onclick="openModal('businessBudgetLine')">编辑</button><button class="btn link" onclick="showToast('已上移')">上移</button><button class="btn link" onclick="showToast('已下移')">下移</button><button class="btn link danger-link" onclick="showToast('演示中已删除该行')">删除</button>`],
        ["2", "现场搭建服务", "1", "项", "26000", "CNY", "¥26,000", "含运输与安装", `<button class="btn link" onclick="openModal('businessBudgetLine')">编辑</button><button class="btn link" onclick="showToast('已上移')">上移</button><button class="btn link" onclick="showToast('已下移')">下移</button><button class="btn link danger-link" onclick="showToast('演示中已删除该行')">删除</button>`],
      ])}
      <div class="notice" style="margin-top:14px;">小计由数量 × 单价自动计算；币种默认 CNY，可按行维护。附件不允许上传可执行文件、脚本和高风险文件。</div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('business-prs')">取消</button>
      <button class="btn" onclick="showToast('采购申请单草稿已保存')">保存草稿</button>
      <button class="btn primary" onclick="state.selectedPr='PR-202605-026'; setPage('business-pr-detail')">提交审批并查看跟踪</button>
    </div>
  `;
}

function businessPrDetailPage() {
  const pr = findPr();
  const contract = contracts.find((c) => c[2] === pr[0]);
  const relatedPos = relatedPoRowsForPr(pr[0]);
  const canCreatePo = contract && (state.contractStatusByNo[contract[0]] || contract[6]) === "合同完成";
  return `
    ${pageHead("采购申请单跟踪", "业务人员查看本人 PR 的审批、采购员分配、合同、PO 和付款闭环进展；RFQ 属于采购内部过程，本页不展示。", `
      <button class="btn" onclick="setPage('business-prs')">返回我的采购申请</button>
      <button class="btn primary" onclick="setPage('business-pr-create')">创建新的PR</button>
    `)}
    ${summary(`${pr[0]} · ${pr[2]}`, `${tag(prProcessStatus(pr))} ${tag(prContractStatus(pr[0], pr[8]))} ${tag(prPaymentStatus(pr[0]))}`, `申请人：${pr[5]} · 部门：${pr[1]} · 已付款总额：${paidTotalForPr(pr[0])}`)}
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>PR基本信息</h3></div>
        ${infoGrid([
          ["PR编号", pr[0]],
          ["PR主题", pr[2]],
          ["申请日期", pr[0] === "PR-202605-026" ? "2026-05-26" : "2026-05-10"],
          ["申请人", pr[5]],
          ["申请部门", pr[1]],
          ["Enduser", "王敏"],
          ["Department Head", "赵明"],
          ["成本中心", "MKT-2026-BRAND"],
          ["采购类型", pr[0] === "PR-202605-026" ? "业务成本类采购" : "公司费用类采购"],
          ["是否唯一供应商", pr[0] === "PR-202605-026" ? "是" : "否"],
          ["供应商类型", pr[0] === "PR-202605-026" ? "客户指定供应商" : "-"],
          ["客户名称", pr[0] === "PR-202605-026" ? "CDP重点客户A" : "-"],
          ["CDP公司抬头", pr[0] === "PR-202605-026" ? "CDP集团" : "-"],
          ["双盖章销售合同", pr[0] === "PR-202605-026" ? "销售合同_双方盖章版.pdf" : "-"],
          ["CRM合同编号/订单编号", pr[0] === "PR-202605-026" ? "CRM-ORD-202605-8891" : "-"],
          ["项目利润表预算确认", pr[0] === "PR-202605-026" ? "是" : "-"],
          ["产品/服务需求", pr[0] === "PR-202605-026" ? "新品发布会需要定制伴手礼、活动搭建物料和现场执行服务。" : pr[2]],
          ["数量", pr[3]],
          ["预算", pr[4]],
          ["希望完成时间", pr[6]],
          ["审批单号", pr[7] === "审批中" ? "AP-PR-202605-026" : "AP-PR-202605-001"],
          ["分配采购员", assignedBuyerForPr(pr)],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>下游单据</h3></div>
        ${table(["单据类型", "单据编号", "状态", "操作"], [
          ["合同", contract ? contract[0] : "-", tag(prContractStatus(pr[0], pr[8])), contract ? `<button class="btn link" onclick="selectContract('${contract[0]}','${pr[0]}')">查看合同</button>` : "-"],
          ["PO", relatedPos.length ? `${relatedPos.length} 个PO` : "-", tag(poApprovalStatus(relatedPos[0])), relatedPos.length ? `<button class="btn link" onclick="setPage('business-pos')">查看PO</button>` : `<button class="btn link" ${canCreatePo ? "" : "disabled"} onclick="setPage('business-po-create')">创建PO</button>`],
          ["付款", paidTotalForPr(pr[0]), tag(prPaymentStatus(pr[0])), relatedPos.length ? `<button class="btn link" onclick="setPage('business-pos')">查看付款</button>` : "-"],
        ])}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>预算明细</h3><span class="hint">已付款总额：${paidTotalForPr(pr[0])}</span></div>
        ${table(["产品/服务", "数量", "单位", "单价", "货币", "总价", "备注"], [
          ["定制伴手礼套装", "800", "套", "65", "CNY", "¥52,000", "含包装设计"],
          ["现场搭建服务", "1", "项", "26000", "CNY", "¥26,000", "含运输与安装"],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>审批信息</h3></div>
        ${infoGrid([
          ["外部审批编号", pr[7] === "审批中" ? "AP-PR-202605-026" : "AP-PR-202605-001"],
          ["审批状态", tag(pr[7] === "审批中" ? "审批中" : "审批通过")],
          ["审批完成时间", pr[7] === "审批中" ? "待完成" : "2026-05-10 14:30"],
          ["分配采购员", assignedBuyerForPr(pr)],
          ["驳回原因", pr[7] === "审批驳回" ? "预算明细不完整，请补充后重提" : "-"],
        ])}
      </div>
    </div>
  `;
}

function businessContractsPage() {
  const rows = contracts.filter((c) => findPr(c[2])?.[5] === roleMeta.business.name);
  return `
    ${pageHead("我的合同", "查看本人 PR 产生的合同；合同完成后可基于合同创建 PO。")}
    ${tableWrap(["合同编号", "合同名称", "关联PR", "供应商", "金额", "货币", "合同状态", "操作"], rows.map((c) => [
      `<button class="btn link" onclick="selectContract('${c[0]}','${c[2]}')">${c[0]}</button>`,
      c[1],
      `<button class="btn link" onclick="selectPr('${c[2]}','business-pr-detail')">${c[2]}</button>`,
      c[4],
      contractAmount(c[5]),
      contractCurrency(c[5]),
      tag(state.contractStatusByNo[c[0]] || c[6]),
      `<button class="btn link" onclick="selectContract('${c[0]}','${c[2]}')">查看详情</button>`,
    ]))}
  `;
}

function businessPosPage() {
  const ownedPrNos = businessOwnedPrs().map((p) => p[0]);
  const rows = poRows.filter((po) => ownedPrNos.includes(po[1]));
  const completedContracts = contracts.filter((c) => ownedPrNos.includes(c[2]) && (state.contractStatusByNo[c[0]] || c[6]) === "合同完成");
  return `
    ${pageHead("我的PO", "合同签署归档后，业务人员可在合同上创建 PO；PO 审批通过后同步台账系统并回传付款结果。")}
    <div class="card">
      <div class="card-title"><h3>可创建PO的合同</h3><span class="hint">仅合同完成后开放</span></div>
      ${table(["合同编号", "关联PR", "供应商", "合同金额", "付款状态", "操作"], completedContracts.map((c) => [
        c[0],
        `<button class="btn link" onclick="selectPr('${c[2]}','business-pr-detail')">${c[2]}</button>`,
        c[4],
        c[5],
        tag(prPaymentStatus(c[2])),
        `<button class="btn primary" onclick="state.selectedPr='${c[2]}'; state.selectedContract='${c[0]}'; setPage('business-po-create')">创建PO</button>`,
      ]))}
      ${completedContracts.length ? "" : `<div class="empty">暂无可创建 PO 的已完成合同。</div>`}
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>PO跟踪列表</h3></div>
      ${tableWrap(["PO编号", "关联PR", "合同编号", "供应商", "PO金额", "货币", "PO状态", "付款状态", "已付款金额", "操作"], rows.map((po) => [
        `<button class="btn link" onclick="selectPo('${po[0]}')">${po[0]}</button>`,
        `<button class="btn link" onclick="selectPr('${po[1]}','business-pr-detail')">${po[1]}</button>`,
        po[2],
        po[4],
        po[5],
        poCurrency(po[5]),
        tag(poApprovalStatus(po)),
        tag(po[7]),
        formatCny(paidAmountForPo(po)),
        `<button class="btn link" onclick="selectPo('${po[0]}')">查看详情</button>`,
      ]))}
      ${rows.length ? "" : `<div class="empty">暂无已创建的 PO。</div>`}
    </div>
  `;
}

function businessPoCreatePage() {
  const contract = findContract() || contracts.find((c) => findPr(c[2])?.[5] === roleMeta.business.name && c[6] === "合同完成") || contracts[6];
  const pr = findPr(contract[2]);
  return `
    ${pageHead("创建PO", "业务人员基于已归档合同创建 PO，提交 Worklife BPM 审批；审批通过后同步台账系统。", `
      <button class="btn" onclick="setPage('business-pos')">返回我的PO</button>
    `)}
    ${summary(`来源合同：${contract[0]} · ${contract[1]}`, `${tag("合同完成")} ${tag("PO草稿", "gray")}`, `关联 PR：${contract[2]} · 供应商：${contract[4]}`)}
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>PO基本信息</h3></div>
        <div class="form-grid">
          ${field("来源合同", contract[0], true)}
          ${field("关联PR", pr[0], true)}
          ${field("供应商", contract[4], true)}
          ${field("合同金额", contractAmount(contract[5]), true)}
          ${field("货币", contractCurrency(contract[5]), true)}
          ${selectField("PO类型", ["费用类PO", "业务类PO"])}
          ${field("成本中心", "MKT-2026-BRAND")}
          ${field("销售订单号", "业务类PO时必填")}
          ${field("付款条款", "验收完成后 30 天付款", true)}
          ${field("PO支撑附件", "验收计划.pdf")}
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>审批与付款信息</h3></div>
        ${infoGrid([
          ["PO状态", tag(state.poApprovalStatus || "草稿")],
          ["付款状态", tag(state.paymentStatus)],
          ["创建人", roleMeta.business.name],
          ["审批系统", "Worklife BPM 审批系统"],
          ["台账系统", "审批通过后自动同步"],
        ])}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>PO明细</h3><button class="btn" onclick="openModal('businessPoLine')">添加明细</button></div>
      ${table(["项目", "数量", "单位", "单价", "金额", "币种", "归集对象", "操作"], [
        ["定制伴手礼套装", "800", "套", "65", "¥52,000", "CNY", "MKT-2026-BRAND", `<button class="btn link" onclick="openModal('businessPoLine')">编辑</button><button class="btn link danger-link" onclick="showToast('已删除该PO明细')">删除</button>`],
        ["现场搭建服务", "1", "项", "26000", "¥26,000", "CNY", "MKT-2026-BRAND", `<button class="btn link" onclick="openModal('businessPoLine')">编辑</button><button class="btn link danger-link" onclick="showToast('已删除该PO明细')">删除</button>`],
      ])}
      <div class="notice" style="margin-top:14px;">费用类 PO 必须填写成本中心；业务类 PO 必须关联销售订单。PO 可拆分创建，金额合计不得超过合同可用金额。</div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('business-pos')">取消</button>
      <button class="btn" onclick="state.poApprovalStatus='草稿'; showToast('PO草稿已保存')">保存草稿</button>
      <button class="btn primary" onclick="state.poApprovalStatus='审批中'; state.poStatus='未生成'; state.paymentStatus='未付款'; setPage('business-pos')">提交 Worklife BPM 审批</button>
    </div>
  `;
}

function buyerDashboard() {
  return `
    ${pageHead("采购员工作台", "集中处理供应商审核、PR、询价单和核价待办。", `
      <button class="btn" onclick="setPage('supplier-create')">创建供应商</button>
      <button class="btn primary" onclick="setPage('create-rfq')">创建询价单</button>
    `)}
    ${metrics([
      ["待审核", "5", "进入审核中心", "reviews"],
      ["待处理 PR", "3", "查看采购申请单", "purchase-requests"],
      ["报价中 RFQ", "12", "查看询价单列表", "rfqs"],
      ["待核价", "3", "进入核价处理", "price-review"],
    ])}
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card clickable-card" role="button" tabindex="0" onclick="if(!event.target.closest('button,input,select,textarea')) setPage('suppliers')" onkeydown="if(event.key==='Enter'){setPage('suppliers')}">
        <div class="card-title"><h3>供应商统计</h3><button class="btn link" onclick="setPage('suppliers')">查看全部</button></div>
        ${metrics([["合作中供应商", "32", "查看合作中供应商", "suppliers"]])}
      </div>
      <div class="card">
        <div class="card-title"><h3>快捷操作</h3></div>
        <div class="quick-grid">
          <button class="quick-action" onclick="setPage('supplier-create')"><strong>创建供应商</strong><span>录入供应商主数据和联系人</span></button>
          <button class="quick-action" onclick="setPage('create-rfq')"><strong>创建询价单</strong><span>基于 PR 发起询价或代录报价</span></button>
          <button class="quick-action" onclick="setPage('contract-create')"><strong>创建合同申请</strong><span>选择已审批完成 RFQ</span></button>
        </div>
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card clickable-card" role="button" tabindex="0" onclick="if(!event.target.closest('button,input,select,textarea')) setPage('rfqs')" onkeydown="if(event.key==='Enter'){setPage('rfqs')}">
        <div class="card-title"><h3>询价单统计</h3><button class="btn link" onclick="setPage('rfqs')">查看全部</button></div>
        ${metrics([["报价中", "12", "查看报价中", "rfqs"], ["核价中", "3", "查看核价中", "price-review"], ["审批中", state.rfqStatus === "审批中" ? "1" : "2", "查看审批中", "rfqs"]])}
      </div>
      <div class="card clickable-card" role="button" tabindex="0" onclick="if(!event.target.closest('button,input,select,textarea')) setPage('reviews')" onkeydown="if(event.key==='Enter'){setPage('reviews')}">
        <div class="card-title"><h3>待审核事项</h3><button class="btn link" onclick="setPage('reviews')">进入审核中心</button></div>
        ${table(["审核类型", "供应商名称", "提交人", "提交时间", "状态", "操作"], [
          ["信息变更", "山西德利仁信息技术服务有限公司", "高阳阳", "2026-05-05 09:30", tag("待审核信息"), `<button class="btn link" onclick="setPage('review-info-detail')">查看</button>`],
          ["证件审核", "上海博夷信息技术有限公司", "沈家伟", "2026-05-05 10:20", tag("待审核信息"), `<button class="btn link" onclick="setPage('review-cert-detail')">查看</button>`],
        ])}
      </div>
      <div class="card clickable-card" role="button" tabindex="0" onclick="if(!event.target.closest('button,input,select,textarea')) setPage('suppliers')" onkeydown="if(event.key==='Enter'){setPage('suppliers')}">
        <div class="card-title"><h3>证件即将到期</h3><button class="btn link" onclick="setPage('suppliers')">查看供应商</button></div>
        ${table(["供应商", "证件类型", "到期日期", "剩余天数", "操作"], [
          ["厦门玉树陶朱文化发展有限公司", "营业执照", "2026-05-22", tag("17天", "orange"), `<button class="btn link" onclick="setPage('supplier-detail')">详情</button>`],
          ["上海乐尔芙农业科技有限公司", "完税证明", "2026-05-05", tag("今日到期", "red"), `<button class="btn link" onclick="setPage('supplier-detail')">详情</button>`],
        ])}
      </div>
    </div>
  `;
}

function suppliersPage() {
  const f = currentFilters();
  const rows = suppliers.filter((s) =>
    textIncludes([s.id, s.name], f.f0) &&
    exactMatch(s.status, f.f1) &&
    exactMatch(s.cert, f.f2) &&
    textIncludes([s.contact], f.f3)
  );
  return `
    ${pageHead("供应商管理", "查看和维护采购员管理范围内的供应商主数据。", `<button class="btn primary" onclick="setPage('supplier-create')">创建供应商</button>`)}
    ${filters([
      "企业名称 / 供应商ID",
      { label: "供应商状态", options: stateOptions.supplier },
      { label: "证件到期状态", options: stateOptions.certExpiry },
      "主要联系人",
    ])}
    ${tableWrap(["供应商ID", "企业名称", "状态", "主要联系人", "联系电话", "证件状态", "关联采购员", "更新时间", "操作"], rows.map((s) => [
      s.id,
      `<button class="btn link" onclick="setPage('supplier-detail')">${s.name}</button>`,
      tag(s.status),
      s.contact,
      s.phone,
      tag(s.cert),
      s.buyer,
      s.updated,
      `<button class="btn link" onclick="setPage('supplier-detail')">详情</button><button class="btn link" onclick="openModal('supplierActivated')">设为合作中</button><button class="btn link danger-link" onclick="openModal('supplierDisabled')">停用</button>`,
    ]))}
  `;
}

function supplierDetail() {
  const tabs = [
    ["info", "企业信息"],
    ["contacts", "联系人"],
    ["certs", "证件"],
    ["changes", "变更记录"],
    ["mails", "邀请/邮件记录"],
  ];
  return `
    ${summary("上海博夷信息技术有限公司", `${tag("合作中")} ${tag("正常")}`, "供应商ID：VD 0001 · 主要联系人：沈家伟 / 13321832330", `
      <button class="btn" onclick="setPage('supplier-edit')">编辑</button><button class="btn" onclick="openModal('supplierActivated')">设为合作中</button><button class="btn danger" onclick="openModal('supplierDisabled')">停用</button>
    `)}
    <div class="card">
      <div class="tabs">
        ${tabs.map(([key, label]) => `<button class="tab-btn ${state.supplierTab === key ? "active" : ""}" onclick="state.supplierTab='${key}'; render()">${label}</button>`).join("")}
      </div>
      ${supplierTabContent()}
    </div>
  `;
}

function supplierTabContent() {
  if (state.supplierTab === "contacts") {
    return `
      <div class="card-title"><h3>联系人</h3><button class="btn primary" onclick="setPage('supplier-contact-edit')">新增联系人</button></div>
      ${table(["姓名", "职务", "部门", "手机号", "邮箱", "主要联系人", "操作"], [
        ["沈家伟", "总经理", "业务部", "13321832330", "jerry.shen@iboye.com", tag("是", "green"), `<button class="btn link" onclick="setPage('supplier-contact-edit')">编辑</button><button class="btn link" onclick="openModal('inviteSent')">发送邀请邮件</button>`],
        ["邹钰", "会计主管", "财务部", "18651960886", "zoe.zou@insee-tech.com", tag("否", "gray"), `<button class="btn link" onclick="setPage('supplier-contact-edit')">编辑</button><button class="btn link" onclick="openModal('inviteSent')">发送邀请邮件</button>`],
      ])}
      <div class="notice" style="margin-top:14px;">采购员和采购经理可直接新增或编辑供应商联系人，无需审批；发送邀请邮件会向联系人邮箱发送供应商门户登录邀请。</div>
    `;
  }
  if (state.supplierTab === "certs") {
    return `
      <div class="card-title">
        <h3>供应商证件</h3>
        <button class="btn primary" onclick="setPage('buyer-cert-upload')">手动添加证件</button>
      </div>
      ${table(["证件类型", "文件名", "有效期开始", "有效期截止", "审核状态", "到期状态", "来源", "维护人", "操作"], [
        ["营业执照", "营业执照_上海博夷.pdf", "2016-01-07", "2036-01-06", tag("已通过"), tag("正常"), tag("供应商上传", "gray"), "沈家伟", `<button class="btn link" onclick="setPage('review-cert-detail')">预览</button><button class="btn link" onclick="setPage('buyer-cert-upload')">更新</button>`],
        ["完税证明", "完税证明_2025.pdf", "2025-01-01", "2026-05-20", tag("已通过"), tag("即将到期"), tag("采购员维护", "blue"), "张伟", `<button class="btn link" onclick="setPage('review-cert-detail')">预览</button><button class="btn link" onclick="setPage('buyer-cert-upload')">更新</button>`],
      ])}
      <div class="notice" style="margin-top:14px;">采购员手动添加或更新的证件默认直接生效为已通过，并记录来源、维护人和维护时间。</div>
    `;
  }
  if (state.supplierTab === "changes") {
    return table(["变更时间", "变更人", "字段名", "变更前", "变更后", "审核状态", "审核人"], [
      ["2026-04-26 10:24", "采购员 张伟", "开户银行", "招商银行上海分行", "招商银行股份有限公司上海虹桥商务区支行", tag("已通过"), "张伟"],
      ["2026-04-18 15:12", "供应商 沈家伟", "主力客户", "威高集团、宝尊电商", "威高集团、宝尊电商、唯德康、万鲤商联、长安金融", tag("已通过"), "李娜"],
    ]);
  }
  if (state.supplierTab === "mails") {
    return table(["邮件类型", "收件人", "邮箱", "状态", "发送时间"], [
      ["邀请入驻", "沈家伟", "jerry.shen@iboye.com", tag("成功", "green"), "2026-04-12 09:10"],
      ["证件到期提醒", "沈家伟", "jerry.shen@iboye.com", tag("成功", "green"), "2026-05-01 08:00"],
    ]);
  }
  return `
    <div class="grid">
      <div>
        <div class="card-title"><h3>基本信息</h3></div>
        ${infoGrid([
          ["供应商名称", "上海博夷信息技术有限公司"],
          ["公司法人", "沈家伟"],
          ["注册时间", "2016-01-07"],
          ["注册资金", "3,000,000 元"],
          ["公司地址", "上海市奉贤区奉浦大道1599号第一幢第一层1-17室"],
          ["一般纳税人", "是"],
        ])}
      </div>
      <div>
        <div class="card-title"><h3>扩展信息</h3></div>
        ${infoGrid([
          ["企业性质", "私营"],
          ["销售模式", "代理商"],
          ["覆盖区域", "华东、华北"],
          ["员工人数", "86"],
          ["本年度营业额", "12,000,000 元"],
          ["主力客户", "威高集团、宝尊电商、唯德康、万鲤商联、长安金融"],
        ])}
      </div>
      <div>
        <div class="card-title"><h3>银行信息</h3></div>
        ${infoGrid([
          ["户名", "上海博夷信息技术有限公司"],
          ["开户银行", "招商银行股份有限公司上海虹桥商务区支行"],
          ["银行账号", "121919443310602"],
        ])}
      </div>
    </div>
  `;
}

function reviewsPage() {
  const f = currentFilters();
  const source = {
    todo: state.role === "admin" ? [
      ["核价审批", "RFQ-202605-001 · 2026 年品牌活动礼品采购", "张伟", "2026-05-13 14:20", state.rfqStatus === "已完成" ? "已通过" : "待审批", "未超时", "admin-approval-detail"],
      ["合同申请审批", "CON-202605-002 · 办公室保洁外包合同", "李娜", "2026-05-14 10:00", "外部审批推送中", "未超时", "admin-contract-detail"],
    ] : [
      ["供应商信息变更", "山西德利仁信息技术服务有限公司", "高阳阳", "2026-05-05 09:30", "待审核信息", "未超时", "review-info-detail"],
      ["供应商证件审核", "上海博夷信息技术有限公司", "沈家伟", "2026-05-05 10:20", "待审核信息", "未超时", "review-cert-detail"],
    ],
    mine: state.role === "admin" ? [
      ["合同申请审批", "CON-202605-003 · 活动直播服务合同", "周敏", "2026-05-13 11:30", "Worklife BPM审批中", "未超时", "admin-contract-detail"],
      ["采购员账号变更", "李娜 · 权限范围调整", "周敏", "2026-05-12 16:20", "已完成", "未超时", "admin-buyers"],
    ] : [
      ["核价审批", "RFQ-202605-001 · 品牌活动礼品采购", "张伟", "2026-05-13 14:20", state.rfqStatus === "审批中" ? "审批中" : "已完成", "未超时", "approval-status"],
      ["合同申请审批", "CON-202605-002 · IT外包服务合同", "张伟", "2026-05-12 11:30", "Worklife BPM审批中", "未超时", "contract-detail"],
    ],
    done: [
      ["供应商证件审核", "上海乐尔芙农业科技有限公司", "程琳", "2026-05-04 08:10", "已驳回", "已超时", "review-cert-detail"],
      ["供应商信息变更", "上海博夷信息技术有限公司", "沈家伟", "2026-04-26 10:24", "已通过", "未超时", "review-info-detail"],
    ],
  };
  const rows = source[state.reviewTab] || source.todo;
  const filteredRows = rows.filter((r) =>
    textIncludes([r[1], r[2]], f.f0) &&
    exactMatch(r[0], f.f1) &&
    textIncludes([r[3]], f.f2) &&
    exactMatch(r[4], f.f3)
  );
  return `
    ${pageHead("审批中心", "按我发起的、需要我审批的、审批完成三类跟踪审批事项；默认进入需要我审批。")}
    <div class="tabs">
      ${[["todo", "需要我审批"], ["mine", "我发起的"], ["done", "审批完成"]].map(([key, label]) => `<button class="tab-btn ${state.reviewTab === key ? "active" : ""}" onclick="state.reviewTab='${key}'; render()">${label}</button>`).join("")}
    </div>
    ${filters([
      "单据 / 供应商 / 提交人",
      { label: "审批类型", options: ["供应商信息变更", "供应商证件审核", "核价审批", "合同申请审批"] },
      "提交时间",
      { label: "审批状态", options: stateOptions.reviewStatus.concat(["审批中", "Worklife BPM审批中"]) },
    ])}
    ${tableWrap(["审批类型", "单据/对象", "提交人", "提交时间", "当前状态", "超时标识", "操作"], filteredRows.map((r) => [
      r[0], r[1], r[2], r[3], tag(r[4]), tag(r[5], r[5] === "已超时" ? "red" : "green"), `<button class="btn link" onclick="setPage('${r[6]}')">查看</button>`,
    ]))}
  `;
}

function legacyReviewsPage() {
  const f = currentFilters();
  const rows = [
    ["信息变更", "山西德利仁信息技术服务有限公司", "高阳阳", "2026-05-05 09:30", "待审核信息", "未超时", "review-info-detail"],
    ["证件审核", "上海博夷信息技术有限公司", "沈家伟", "2026-05-05 10:20", "已通过", "未超时", "review-cert-detail"],
    ["证件审核", "上海乐尔芙农业科技有限公司", "程琳", "2026-05-04 08:10", "已驳回", "已超时", "review-cert-detail"],
  ].filter((r) =>
    textIncludes([r[1], r[2]], f.f0) &&
    exactMatch(r[0], f.f1) &&
    textIncludes([r[3]], f.f2) &&
    exactMatch(r[4], f.f3)
  );
  return `
    ${pageHead("审核中心", "统一处理供应商信息变更和证件审核。")}
    ${metrics([["待审核总数", "5"], ["信息变更", "2"], ["证件审核", "3"], ["超 24 小时未审", "1"]])}
    <div style="height:16px"></div>
    ${filters([
      "供应商名称",
      { label: "审核类型", options: stateOptions.reviewType },
      "提交时间",
      { label: "审核状态", options: stateOptions.reviewStatus },
    ])}
    ${tableWrap(["审核类型", "供应商名称", "提交人", "提交时间", "当前状态", "超时标识", "操作"], rows.map((r) => [
      r[0], r[1], r[2], r[3], tag(r[4]), tag(r[5], r[5] === "已超时" ? "red" : "green"), `<button class="btn link" onclick="setPage('${r[6]}')">查看</button>`,
    ]))}
  `;
}

function purchaseRequestsPage() {
  const f = currentFilters();
  const rows = prs.map((p, i) => ({ p, i })).filter(({ p, i }) => {
    const prStatus = prProcessStatus(p);
    return textIncludes([p[0]], f.f0) &&
      textIncludes([p[2]], f.f1) &&
      textIncludes([p[5]], f.f2) &&
      textIncludes([p[1]], f.f3) &&
      exactMatch(prStatus, f.f4);
  });
  return `
    ${pageHead("采购申请单", "业务人员在采购系统发起 PR，Worklife BPM 审批通过并分配采购员后进入采购流程；多张 PR 可合并为 PR 合集后进入询价流程。", `
      <button class="btn" onclick="setPage('create-rfq')">创建询价管理单</button>
      <button class="btn primary" onclick="openModal('mergePrBundle')">创建PR合集</button>
    `)}
    <div class="notice" style="margin-bottom:14px;">当前列表字段来自「表单设置 / 采购申请单表单」。搜索栏仅查询 PR 本身字段；合同状态、PO状态、付款状态保留在列表展示和详情页中，不作为本页搜索项。</div>
    ${filters([
      "采购申请单号",
      "主题",
      "申请人",
      "申请部门",
      { label: "PR单状态", options: stateOptions.pr },
    ])}
    ${tableWrap(["PR号", "PR类型", "部门", "内容摘要", "预算", "希望完成时间", "PR状态", "合同状态", "PO状态", "付款状态", "已付款总额", "操作"], rows.map(({ p, i }) => {
      return [
        `<button class="btn link" onclick="selectPr('${p[0]}')">${p[0]}</button>`, tag(prType(p), isPrBundle(p) ? "purple" : "gray"), p[1], p[2], p[4], p[6], tag(prProcessStatus(p)), tag(prContractStatus(p[0], p[8])), tag(poApprovalStatus(relatedPoRowsForPr(p[0])[0])), tag(prPaymentStatus(p[0])), paidTotalForPr(p[0]), `<button class="btn link" onclick="selectPr('${p[0]}')">查看详情</button><button class="btn link" onclick="selectPr('${p[0]}','create-rfq')">创建询价管理单</button>`,
      ];
    }))}
  `;
}

function rfqsPage() {
  const f = currentFilters();
  const rows = rfqs.map((r, i) => ({ r, i })).filter(({ r, i }) => {
    const status = i === 0 ? state.rfqStatus : r[5];
    return textIncludes([r[0], r[1], r[2]], f.f0) &&
      exactMatch(status, f.f1) &&
      exactMatch(r[3], f.f2) &&
      exactMatch(r[4], f.f3);
  });
  return `
    ${pageHead("询价单管理", "管理从 RFQ 创建、报价、开标、核价到审批完成的全流程。", `
      <button class="btn primary" onclick="setPage('create-rfq')">创建询价单</button>
    `)}
    <div class="notice" style="margin-bottom:14px;">线下寻源独立入口已删除。请统一点击「创建询价单」，通过采购策略区分询比价、单一来源、定向采购和续约。</div>
    ${filters([
      "项目名称 / PR号",
      { label: "RFQ状态", options: stateOptions.rfq },
      { label: "采购策略", options: stateOptions.strategy },
      { label: "报价方式", options: stateOptions.quoteMode },
    ])}
    ${tableWrap(["RFQ编号", "项目名称", "PR号", "采购策略", "报价方式", "状态", "轮次", "供应商数", "操作"], rows.map(({ r, i }) => [
      r[0], `<button class="btn link" onclick="selectRfq('${r[0]}')">${r[1]}</button>`, r[2], r[3], tag(r[4]), tag(i === 0 ? state.rfqStatus : r[5]), r[6], r[7], `<button class="btn link" onclick="selectRfq('${r[0]}')">查看</button><button class="btn link" onclick="selectRfq('${r[0]}','rfq-edit')">编辑</button>`,
    ]))}
  `;
}

function createRfqPage() {
  const stepHtml = [
    createStepPr,
    createStepBase,
    createStepSchedule,
    createStepSuppliers,
    createStepConfirm,
  ][state.createStep - 1]();
  return `
    ${pageHead("创建询价单", "统一从 PR 或 PR 合集创建 RFQ；删除 RFQ 类型，通过采购策略决定供应商在线报价或采购员代录报价。")}
    <div class="card">
      ${steps(["选择采购申请单", "项目基本信息", "报价安排", "参与供应商", "确认发布"], state.createStep)}
      ${stepHtml}
      <div class="sticky-actions">
        <button class="btn" onclick="setPage('purchase-requests')">取消</button>
        <button class="btn" ${state.createStep === 1 ? "disabled" : ""} onclick="state.createStep=Math.max(1,state.createStep-1); render()">上一步</button>
        ${state.createStep < 5 ? `<button class="btn primary" onclick="state.createStep=Math.min(5,state.createStep+1); render()">下一步</button>` : `
          <button class="btn" onclick="state.rfqStatus='草稿'; setPage('rfq-detail')">保存草稿</button>
          <button class="btn primary" onclick="state.rfqStatus='报价中'; setPage('rfq-detail')">询比价发布并通知</button>
          <button class="btn" onclick="state.rfqStatus='报价中'; setPage('offline-sourcing')">保存并进入代录报价</button>
        `}
      </div>
    </div>
  `;
}

function offlineSourcingPage() {
  const backPage = state.role === "admin" ? "admin-rfqs" : "rfqs";
  const finishAction = state.role === "admin"
    ? `state.rfqStatus='已开标'; setPage('bid-summary')`
    : `state.rfqStatus='已开标'; setPage('bid-summary')`;
  return `
    ${pageHead("采购员代录报价", "用于单一来源、定向采购、续约策略；不通知供应商，由采购员录入供应商报价并上传附件。", `
      <button class="btn" onclick="setPage('${backPage}')">返回询价单查询</button>
      <button class="btn primary" onclick="${finishAction}">关闭报价</button>
    `)}
    ${summary("代录供应商报价", `${tag("采购员代录报价")} ${tag("报价中")}`, "适用于单一来源、定向采购、续约等不开放供应商端报价的采购场景")}
    <div class="card">
      ${steps(["关联采购申请单", "录入线下报价", "关闭报价"], 2)}
      <div class="form-grid">
        ${selectField("关联采购申请单", ["PRG-202605-001 / 办公与活动物料采购合集", "PR-202605-003 / 上海办公室绿植租摆服务", "PR-202605-008 / 仓储标签打印设备", "PR-202605-007 / 线下活动摄影服务"])}
        ${field("RFQ编号", "系统保存后自动生成", true)}
        ${field("项目名称", "上海办公室绿植租摆服务")}
        ${selectField("采购策略", ["单一来源", "定向采购", "续约"])}
        ${field("采购员", "张伟")}
        ${field("采购公司", "CDP集团")}
        ${textareaField("代录说明", "供应商通过邮件提供报价，采购员在系统内代录报价明细并上传报价附件，用于后续核价审批和合同流程。")}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>供应商报价</h3><div class="btn-row"><a class="btn" href="file:///Users/qing/Documents/caigou/供应商报价单模版.xlsx">下载模板</a><button class="btn primary" onclick="openModal('offlineQuote')">代录供应商报价</button></div></div>
      ${table(["供应商", "联系人", "电话/邮箱", "报价附件", "物料/服务", "数量", "单位", "未税单价", "税率", "含税小计", "状态", "操作"], [
        ["上海博夷信息技术有限公司", "沈家伟", "13321832330 / jerry.shen@iboye.com", "上海博夷_报价邮件.pdf", "云监控订阅服务", "12", "月", "¥5,030", "6%", "¥64,000", tag("已代录", "green"), `<button class="btn link" onclick="openModal('offlineQuote')">编辑</button>`],
      ])}
      <div class="notice" style="margin-top:14px;">代录报价必须上传供应商报价附件。保存后系统不发送供应商在线报价待办，也不触发 RFQ 发布邮件；点击「关闭报价」后，系统记录报价截止流水并立即进入已开标流程。</div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('${backPage}')">取消</button>
      <button class="btn">保存草稿</button>
      <button class="btn primary" onclick="${finishAction}">关闭报价</button>
    </div>
  `;
}

function createStepPr() {
  const f = currentFilters();
  const rows = [
    [`<input type="radio" checked />`, "PR-202605-001", tag("普通PR", "gray"), "市场部", "2026 年品牌活动礼品采购", "¥150,000", "王敏", "2026-05-30"],
    [`<input type="radio" />`, "PRG-202605-001", tag("PR合集", "purple"), "多部门", "办公与活动物料采购合集", "¥204,000", "张伟", "2026-06-12"],
    [`<input type="radio" />`, "PR-202605-003", tag("普通PR", "gray"), "行政部", "上海办公室绿植租摆服务", "¥60,000", "刘珊", "2026-05-25"],
    [`<input type="radio" />`, "PR-202605-008", tag("普通PR", "gray"), "运营部", "仓储标签打印设备", "¥72,000", "赵磊", "2026-06-05"],
    [`<input type="radio" />`, "PRG-202605-002", tag("PR合集", "purple"), "多部门", "办公易耗品临时采购合集", "¥20,000", "张伟", "2026-06-08"],
  ].filter((row) =>
    textIncludes([row[1], row[4]], f.f0) &&
    textIncludes([row[3]], f.f1) &&
    (!f.f2 || stripHtml(row[2]).includes(f.f2))
  );
  return `
    <div class="notice">选择已审批通过并分配给当前采购员的采购申请单或采购申请单合集，系统会自动带出预算、部门、申请人和需求内容；业务成本类 PR 同时带出 CDP 公司抬头。PR 合集与普通 PR 一样可以作为 RFQ 来源。</div>
    <div style="height:14px"></div>
    ${filters([
      "PR号 / 需求内容",
      "部门",
      { label: "PR类型", options: ["普通PR", "PR合集"] },
    ])}
    ${table(["选择", "PR号", "类型", "部门", "内容", "预算", "申请人", "希望完成时间"], rows)}
  `;
}

function createStepBase() {
  return `
    <div class="form-grid">
      ${field("项目名称", "2026 年品牌活动礼品采购")}
      ${field("PR号", "PR-202605-001", true)}
      ${field("项目预算", "150000")}
      ${selectField("是否向供应商展示预算", ["否", "是"])}
      ${selectField("CDP公司抬头", ["CDP China Holding", "CDP Shanghai", "CDP Beijing"])}
      ${field("采购员", "张伟", true)}
      ${textareaField("项目需求概述", "采购定制帆布袋与礼盒包装服务，用于 2026 年品牌活动。")}
    </div>
    <div style="height:14px"></div>
    ${buyerQuoteContentSettings()}
  `;
}

function createStepSchedule() {
  return `
    <div class="form-grid">
      ${selectField("采购策略", ["询比价", "单一来源", "定向采购", "续约"])}
      ${field("可报价开始时间", "2026-05-06 09:00")}
      ${field("可报价结束时间", "2026-05-12 18:00")}
      ${field("开标时间", "2026-05-13 10:00")}
      <div class="notice full">校验规则：开标时间必须晚于报价结束时间；单一来源、定向采购、续约只能选择一家供应商，且不通知供应商、不生成供应商端报价待办。</div>
    </div>
  `;
}

function buyerQuoteContentSettings() {
  return `
    <div class="card soft">
      <div class="card-title"><h3>报价内容设置</h3><button class="btn" onclick="openModal('buyerQuoteLine')">添加报价明细行</button></div>
      <div class="notice" style="margin-bottom:14px;">采购员在询价单中先设置本次希望供应商报价的标准明细行。供应商报价时默认带出这些行，同时允许供应商新增自由报价行；核价时可将供应商自由报价行映射回标准明细行进行比价。</div>
      ${table(["序号", "物料/服务名称", "数量", "单价", "小计", "服务/交货天数", "备注", "操作"], [
        ["1", `<input class="input" value="定制帆布袋" />`, `<input class="input" value="3000" />`, `<input class="input" value="18.00" />`, `<input class="input" value="54,000.00" readonly />`, `<input class="input" value="18天" />`, `<input class="input" value="带品牌 LOGO，含打样" />`, `<button class="btn link" onclick="openModal('buyerQuoteLine')">编辑</button><button class="btn link danger" onclick="showToast('已删除该报价明细行')">删除</button>`],
        ["2", `<input class="input" value="礼盒包装服务" />`, `<input class="input" value="3000" />`, `<input class="input" value="4.50" />`, `<input class="input" value="13,500.00" readonly />`, `<input class="input" value="15天" />`, `<input class="input" value="含包装设计与装箱" />`, `<button class="btn link" onclick="openModal('buyerQuoteLine')">编辑</button><button class="btn link danger" onclick="showToast('已删除该报价明细行')">删除</button>`],
      ])}
      <div class="notice" style="margin-top:14px;">控制规则：至少保留 1 行报价明细；小计 = 数量 × 单价，页面自动计算；删除行需二次确认，已经发布的 RFQ 不允许直接删除已报价明细，只能开启变更记录或新一轮报价。</div>
    </div>
  `;
}

function createStepSuppliers() {
  return `
    <div class="card">
      <div class="card-title"><h3>参与供应商</h3><span class="hint">仅展示合作中且证件状态可参与询价的供应商</span></div>
      <div class="filters compact">
        <div class="filter-fields">
          <input class="input" placeholder="按供应商名称搜索" />
          <select class="select">
            <option>供应商类型</option>
            <option>全部</option>
            <option>代理商</option>
            <option>经销商</option>
            <option>服务商</option>
            <option>生产厂商</option>
          </select>
          <select class="select">
            <option>证件状态</option>
            <option>全部</option>
            <option>正常</option>
            <option>即将到期</option>
          </select>
        </div>
        <div class="filter-actions">
          <button class="btn primary">筛选</button>
          <button class="btn ghost reset-btn">重置</button>
        </div>
      </div>
      ${table(["选择", "供应商名称", "供应商类型", "证件状态", "通知联系人", "适用策略"], [
          [`<input type="checkbox" checked />`, "上海博夷信息技术有限公司", "代理商", tag("正常"), `<select class="select"><option>沈家伟 / jerry.shen@iboye.com</option><option>邹钰 / zoe.zou@insee-tech.com</option></select>`, "询比价 / 单一来源 / 定向采购 / 续约"],
          [`<input type="checkbox" checked />`, "山西德利仁信息技术服务有限公司", "服务商", tag("正常"), `<select class="select"><option>高阳阳 / 1542533389@qq.com</option><option>王宁 / finance@deliren.com</option></select>`, "询比价"],
          [`<input type="checkbox" checked />`, "上海乐尔芙农业科技有限公司", "生产厂商", tag("正常"), `<select class="select"><option>程琳 / chenglin@example.com</option><option>陆佳 / finance@leerfu.com</option></select>`, "询比价"],
          [`<input type="checkbox" />`, "厦门玉树陶朱文化发展有限公司", "经销商", tag("即将到期"), `<select class="select"><option>沈荣峰 / shenrf@example.com</option><option>陈佳 / chen.jia@example.com</option></select>`, "询比价"],
        ])}
      <div class="notice" style="margin-top:14px;">询比价：可选择多家供应商，必须指定通知联系人并发送询价邮件。单一来源/定向采购/续约：仅允许保留一家供应商，不发送邮件，保存后进入「报价中」，由采购员代录报价。</div>
    </div>
  `;
}

function createStepConfirm() {
  return `
    <div class="grid cols-2">
      <div class="notice"><strong>RFQ 摘要</strong><br/>项目：2026 年品牌活动礼品采购<br/>PR：PR-202605-001<br/>预算：¥150,000<br/>CDP公司抬头：CDP China Holding<br/>策略：询比价</div>
      <div class="notice"><strong>报价安排</strong><br/>报价时间：2026-05-06 09:00 ~ 2026-05-12 18:00<br/>开标时间：2026-05-13 10:00<br/>参与供应商：3 家<br/>报价方式：供应商在线报价</div>
    </div>
    <div style="height:14px"></div>
    ${createStepSuppliers()}
  `;
}

function rfqDetailPage() {
  const rfq = findRfq();
  const pr = findPr(rfq[2]);
  const status = rfq[0] === "RFQ-202605-001" ? state.rfqStatus : rfq[5];
  const supplierRows = [
    ["上海博夷信息技术有限公司", "沈家伟", "13321832330", "已报价", "2026-05-08 14:20", "否", `<button class="btn link" onclick="setPage('quote-detail')">报价详情</button><button class="btn link" onclick="openModal('returnQuote')">退回</button>`],
    ["山西德利仁信息技术服务有限公司", "高阳阳", "13301126143", "待报价", "-", "否", `<button class="btn link" onclick="setPage('proxy-quote')">代询价</button>`],
    ["上海乐尔芙农业科技有限公司", "程琳", "13901621696", "已报价", "2026-05-08 16:10", "是", `<button class="btn link" onclick="setPage('quote-detail')">报价详情</button>`],
  ];
  return `
    ${summary(`${rfq[0]} · ${rfq[1]}`, `${tag(status)} ${tag(rfq[4])} ${tag(rfq[6], "outline")}`, `${rfq[2]} · 采购策略：${rfq[3]} · 供应商数：${rfq[7]}`, rfqActions(status))}
    <div class="card">
      ${rfqProgress(status)}
      <div style="height:16px"></div>
      ${infoGrid([
        ["项目预算", pr[4]],
        ["是否展示预算", "否"],
        ["采购公司", "CDP集团"],
        ["采购员", "张伟"],
        ["可报价时间", "2026-05-06 09:00 ~ 2026-05-12 18:00"],
        ["开标时间", "2026-05-13 10:00"],
        ["项目需求概述", `${rfq[1]}，来源 ${rfq[2]}，业务申请人 ${pr[5]}。`],
      ])}
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>参与供应商</h3></div>
        ${table(["供应商名称", "通知人", "联系电话", "报价状态", "提交时间", "代询价", "操作"], supplierRows.map((r) => [r[0], r[1], r[2], tag(r[3]), r[4], r[5] === "是" ? tag("代询价", "blue") : tag("否", "gray"), r[6]]))}
      </div>
      <div class="card">
        <div class="card-title"><h3>轮次与邮件记录</h3></div>
        <div class="timeline">
          ${timeline("第1轮报价已发布", "2026-05-06 09:00，通知 3 家供应商")}
          ${timeline("上海博夷已提交报价", "2026-05-08 14:20，邮件通知采购员")}
          ${timeline("上海乐尔芙由采购员代询价", "2026-05-08 16:10，已上传供应商报价附件")}
        </div>
      </div>
    </div>
  `;
}

function rfqActions(status = state.rfqStatus) {
  const contractListPage = state.role === "admin" ? "admin-contracts" : "contracts";
  if (status === "报价中") {
    return `<button class="btn" onclick="setPage('rfq-edit')">编辑询价内容</button><button class="btn primary" onclick="state.rfqStatus='已开标'; setPage('bid-summary')">关闭报价</button><button class="btn danger" onclick="state.rfqStatus='已取消'; render()">取消询价单</button>`;
  }
  if (status === "报价截止") {
    return `<button class="btn primary" onclick="state.rfqStatus='已开标'; setPage('bid-summary')">开标并进入待核价</button><button class="btn danger">取消询价单</button>`;
  }
  if (status === "已开标") {
    return `<button class="btn primary" onclick="setPage('price-review')">进入核价流程</button><button class="btn" onclick="setPage('bid-summary')">查看开标汇总</button><button class="btn" onclick="setPage('new-round')">开启新一轮</button>`;
  }
  if (status === "核价中") {
    return `<button class="btn primary" onclick="setPage('price-review')">进入核价</button>`;
  }
  if (status === "审批中") {
    return `<button class="btn" onclick="setPage('approval-status')">查看审批状态</button>`;
  }
  if (status === "已完成") {
    return `<button class="btn primary" onclick="setPage('${contractListPage}')">进入合同管理创建合同</button>`;
  }
  return `<button class="btn">查看记录</button>`;
}

function rfqProgress(status = state.rfqStatus) {
  const steps = ["草稿", "报价中", "报价截止", "已开标", "核价中", "审批中", "已完成"];
  const idx = Math.max(0, steps.indexOf(status));
  return `<div class="progress">${steps
    .map((x, i) => `<div class="progress-item ${i < idx ? "done" : i === idx ? "active" : ""}"><span class="progress-dot"></span>${x}</div>${i < steps.length - 1 ? '<span class="progress-line"></span>' : ""}`)
    .join("")}</div>`;
}

function bidSummaryPage() {
  state.rfqStatus = state.rfqStatus === "报价截止" ? "已开标" : state.rfqStatus;
  return `
    ${pageHead("已开标 · 开标汇总", "已开标后先查看报价对比，再进入核价流程确定最终中标价。", `
      <button class="btn primary" onclick="state.rfqStatus='核价中'; setPage('price-review')">进入核价流程</button>
      <button class="btn" onclick="setPage('new-round')">开启新一轮报价</button>
      <button class="btn">导出报价对比表</button>
    `)}
    ${summary("RFQ-202605-001 · 第1轮", `${tag("已开标")} ${tag("待核价")} ${tag("已报价供应商 3 家", "green")}`, "项目：2026 年品牌活动礼品采购 · 未报价供应商 0 家")}
    <div class="card">
      <div class="card-title"><h3>物料维度报价对比</h3></div>
      <div class="table-wrap">
        <table class="quote-matrix">
          <thead><tr><th>物料/服务</th><th>上海博夷</th><th>山西德利仁</th><th>上海乐尔芙</th></tr></thead>
          <tbody>
            <tr><td>定制帆布袋</td><td>¥18.00 / ¥54,000 / 15天</td><td class="lowest">¥17.50 / ¥52,500 / 18天 最低价</td><td>¥19.20 / ¥57,600 / 12天</td></tr>
            <tr><td>礼盒包装服务</td><td class="lowest">¥4.50 / ¥13,500 / 15天 最低价</td><td>¥4.80 / ¥14,400 / 18天</td><td>¥4.70 / ¥14,100 / 12天</td></tr>
          </tbody>
        </table>
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>供应商维度汇总</h3></div>
      ${table(["供应商", "未税总额", "含税总额", "税率", "账期", "付款方式", "发票类型", "附件"], [
        ["上海博夷信息技术有限公司", "¥67,500", "¥71,550", "6%", "30天", "后结算", "增值税专用发票", "报价单.pdf"],
        ["山西德利仁信息技术服务有限公司", "¥66,900", "¥70,914", "6%", "45天", "后结算", "增值税专用发票", "报价单.xlsx"],
        ["上海乐尔芙农业科技有限公司", "¥71,700", "¥76,002", "6%", "30天", "预付", "普通发票", "供应商报价附件.pdf"],
      ])}
    </div>
  `;
}

function priceReviewMetricCards() {
  const cards = [
    ["项目预算", "¥150,000", "来自 PR 预算"],
    ["最低报价含税", "¥70,914", "山西德利仁方案"],
    ["最终核价总额", "¥70,914", "已按中标明细汇总"],
    ["预计节省", "¥79,086", "节省率 52.7%"],
  ];
  return `<div class="price-review-metrics">${cards.map(([label, value, rule]) => `
    <div class="metric">
      <div class="label">${label}</div>
      <div class="value">${value}</div>
      <div class="delta">${rule}</div>
    </div>
  `).join("")}</div>`;
}

function priceReviewPage() {
  state.rfqStatus = "核价中";
  return `
    ${pageHead("核价总览", "集中查看报价明细、核价金额和审批信息。", `
      <button class="btn" onclick="setPage('bid-summary')">返回开标汇总</button>
      <button class="btn primary" onclick="openModal('priceReviewConfirm')">提交采购经理审批</button>
    `)}
    <div class="price-review-hero">
      <div>
        <div class="card-title"><h3>RFQ-202605-001 · 2026 年品牌活动礼品采购</h3>${tag("核价中")}</div>
        <div class="price-review-meta">
          <span>PR-202605-001</span>
          <span>采购员：张伟</span>
          <span>参与供应商：3 家</span>
          <span>最终选择供应商：山西德利仁</span>
        </div>
        <div class="price-review-checks">
          ${tag("预算内", "green")}
          ${tag("附件完整", "green")}
          ${tag("待提交审批", "orange")}
        </div>
      </div>
      <div class="price-review-amount">
        <span>最终核价总额</span>
        <strong>¥70,914</strong>
        <em>预算占用 47.3%</em>
      </div>
    </div>
    ${priceReviewMetricCards()}
    <div class="price-review-layout">
      <div class="card price-review-main-card price-review-detail-card">
        <div class="card-title">
          <h3>最终中标明细</h3>
          <div class="btn-row"><span class="hint">来源：山西德利仁信息技术服务有限公司报价</span><button class="btn primary" onclick="openModal('awardLine')">添加行</button></div>
        </div>
        <div class="price-review-table">
        ${table(["供应商", "物料/服务", "数量", "单价", "小计", "税率", "含税小计", "服务/交货天数", "最终成交价", "操作"], [
          ["山西德利仁", "定制帆布袋", "3000", "¥17.50", "¥52,500", "6%", "¥55,650", "18天", "¥17.50 / ¥52,500", `<button class="btn link" onclick="openModal('priceAdjust')">改价</button>`],
          ["山西德利仁", "礼盒包装服务", "3000", "¥4.80", "¥14,400", "6%", "¥15,264", "18天", "¥4.80 / ¥14,400", `<button class="btn link" onclick="openModal('priceAdjust')">改价</button>`],
        ])}
        </div>
      </div>
      <div class="price-review-support">
        <div class="card price-review-supplier-card">
          <div class="card-title"><h3>最终中标供应商选择</h3></div>
          <div class="winner-options compact">
            <label class="winner-option selected">
              <input type="radio" name="winnerSupplier" checked />
              <span><strong>山西德利仁信息技术服务有限公司</strong><em>¥70,914 · 18天 · 45天后结算</em><small>总价最低，交付满足 PR 希望完成时间</small></span>
            </label>
            <label class="winner-option">
              <input type="radio" name="winnerSupplier" />
              <span><strong>上海博夷信息技术有限公司</strong><em>¥71,550 · 15天 · 30天后结算</em><small>交付较快，价格略高</small></span>
            </label>
            <label class="winner-option">
              <input type="radio" name="winnerSupplier" />
              <span><strong>上海乐尔芙农业科技有限公司</strong><em>¥76,002 · 12天 · 预付</em><small>交付最快，但价格和付款条件不优</small></span>
            </label>
          </div>
        </div>
        <div class="card">
          <div class="card-title"><h3>核价结论</h3></div>
          <div class="price-review-decision">
            <div><span>最终中标供应商</span><strong>山西德利仁</strong></div>
            <div><span>最终含税总额</span><strong>¥70,914</strong></div>
            <div><span>PR预算</span><strong>¥150,000</strong></div>
            <div><span>预算占用</span><strong>47.3%</strong></div>
          </div>
          <div class="field price-review-comment">
            <label>核价结论 / 审批说明</label>
            <textarea class="textarea">最终选择山西德利仁信息技术服务有限公司整单中标；总价最低，交付周期满足 PR 要求。</textarea>
          </div>
        </div>
        <div class="card soft">
          <div class="card-title"><h3>改价记录</h3></div>
          <div class="empty">暂无改价记录。</div>
        </div>
      </div>
    </div>
  `;
}

function priceReviewQuotesPage() {
  return priceReviewPage();
}

function approvalStatusPage() {
  const contractListPage = state.role === "admin" ? "admin-contracts" : "contracts";
  return `
    ${pageHead("核价审批状态", "采购员提交核价结果后，可在本页跟踪采购经理审批状态。", `
      <button class="btn" onclick="setPage('rfq-detail')">返回询价单</button>
      <button class="btn primary" onclick="setPage('${contractListPage}')" ${state.rfqStatus === "已完成" ? "" : "disabled"}>进入合同管理</button>
    `)}
    ${summary("RFQ-202605-001 · 2026 年品牌活动礼品采购", `${tag(state.rfqStatus === "已完成" ? "已完成" : "审批中")}`, "提交人：张伟 · 审批人：采购经理 周敏 · 核价总额：¥70,914")}
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>审批流转</h3></div>
        <div class="timeline">
          ${timeline("采购员提交核价审批", "2026-05-13 14:20 · 已提交核价说明与中标建议")}
          ${timeline("等待采购经理审批", state.rfqStatus === "已完成" ? "2026-05-13 15:10 · 审批通过" : "采购经理将在独立的采购经理端处理")}
          ${state.rfqStatus === "已完成" ? timeline("进入待建合同", "采购员可在合同管理中创建合同申请") : ""}
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>核价摘要</h3></div>
        ${table(["物料", "中标供应商", "最终价", "小计"], [
          ["定制帆布袋", "山西德利仁", "¥17.50", "¥52,500"],
          ["礼盒包装服务", "上海博夷", "¥4.50", "¥13,500"],
        ])}
        <div class="notice" style="margin-top:14px;">审批动作仅在采购经理端展示。采购员端只显示审批状态和后续合同入口。</div>
      </div>
    </div>
  `;
}

function adminDashboard() {
  return `
    ${pageHead("采购经理工作台", "以报表视角查看采购规模、支出、流程效率和风险事项。")}
    ${metrics([["本月采购金额", "¥1,286,000", "查看支出分析", "admin-report-spend"], ["进行中采购", "28", "查看流程效率", "admin-report-process"], ["平均核价周期", "1.8天", "查看效率报表", "admin-report-process"], ["异常风险", "6", "查看风险报表", "admin-report-risks"]])}
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>支出概览</h3><button class="btn link" onclick="setPage('admin-report-spend')">查看报表</button></div>
        ${table(["品类", "本月支出", "占比", "趋势"], [
          ["市场活动", "¥420,000", "32.7%", tag("上升", "orange")],
          ["IT与服务", "¥310,000", "24.1%", tag("稳定", "green")],
          ["行政办公", "¥188,000", "14.6%", tag("下降", "green")],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>流程健康度</h3><button class="btn link" onclick="setPage('admin-report-process')">查看效率</button></div>
        ${table(["环节", "当前积压", "平均耗时", "风险"], [
          ["PR待创建RFQ", "3", "0.9天", tag("正常", "green")],
          ["核价审批", state.rfqStatus === "审批中" ? "1" : "0", "1.2天", tag("正常", "green")],
          ["合同BPM审批", "4", "2.6天", tag("关注", "orange")],
        ])}
      </div>
    </div>
  `;
}

function adminApprovalPage(embedded = false) {
  const approvalTag = state.rfqStatus === "已完成" ? tag("已通过", "green") : tag("待审批", "orange");
  const body = `
    <div class="card">
      <div class="tabs">
        ${[["todo", "需要我审批"], ["mine", "我发起的"], ["done", "审批完成"]].map(([key, label]) => `<button class="tab-btn ${state.reviewTab === key ? "active" : ""}" onclick="state.reviewTab='${key}'; render()">${label}</button>`).join("")}
      </div>
      ${summary("RFQ-202605-001 · 2026 年品牌活动礼品采购", `${approvalTag}`, "采购员：张伟 · 预算：¥150,000 · 核价总额：¥70,914")}
      <div class="notice" style="margin: 0 0 14px;">
        关联采购申请单：<button class="btn link" onclick="selectPr('PR-202605-001','pr-detail')">PR-202605-001 · 查看PR详情</button>
      </div>
      <div class="grid cols-2">
        <div>
          <h3>核价结果</h3>
          <div class="card-title"><h3>最终中标供应商选择</h3></div>
          ${table(["选择", "供应商", "最终核价总额", "交付周期", "付款条件"], [
            [`<input type="radio" name="adminWinnerSupplier" checked />`, `${tag("最终选择", "green")} 山西德利仁信息技术服务有限公司`, "¥70,914", "18天", "45天后结算"],
            [`<input type="radio" name="adminWinnerSupplier" />`, "上海博夷信息技术有限公司", "¥71,550", "15天", "30天后结算"],
            [`<input type="radio" name="adminWinnerSupplier" />`, "上海乐尔芙农业科技有限公司", "¥76,002", "12天", "预付"],
          ])}
          <div style="height:12px"></div>
          <div class="card-title"><h3>最终中标明细</h3><button class="btn" onclick="openModal('awardLine')">添加行</button></div>
          ${table(["供应商", "物料/服务", "数量", "单价", "小计", "服务/交货天数", "最终成交价", "操作"], [
            ["山西德利仁", "定制帆布袋", "3000", "¥17.50", "¥52,500", "18天", "¥17.50 / ¥52,500", `<button class="btn link" onclick="openModal('priceAdjust')">改价</button>`],
            ["山西德利仁", "礼盒包装服务", "3000", "¥4.80", "¥14,400", "18天", "¥4.80 / ¥14,400", `<button class="btn link" onclick="openModal('priceAdjust')">改价</button>`],
          ])}
        </div>
        <div>
          <h3>审批依据</h3>
          ${infoGrid([
            ["PR号", `<button class="btn link" onclick="selectPr('PR-202605-001','pr-detail')">PR-202605-001</button>`],
            ["申请部门", "市场部"],
            ["申请人", "王敏"],
            ["采购策略", "询比价"],
            ["参与供应商", "3 家"],
            ["采购员说明", "推荐最终选择山西德利仁信息技术服务有限公司整单中标；总价最低，交付周期满足 PR 要求。"],
          ])}
        </div>
      </div>
      <div class="sticky-actions">
        ${state.rfqStatus === "已完成" ? `<button class="btn primary" onclick="setPage('admin-rfqs')">查看询价单查询</button>` : `<button class="btn danger" onclick="openModal('rejectApproval')">审批驳回</button><button class="btn primary" onclick="state.rfqStatus='已完成'; state.contractStatus='未创建'; openModal('approved')">审批通过</button>`}
      </div>
    </div>
  `;
  return embedded ? body : pageHead("审批中心", "核价审批作为审批中心的一种事项处理，默认进入需要我审批。") + body;
}

function adminDataSettingsPage() {
  return `
    ${pageHead("数据设置", "集中维护采购员账号、字段库和表单模板。")}
    <div class="grid cols-3">
      <div class="card clickable-card" role="button" tabindex="0" onclick="setPage('admin-buyers')">
        <div class="card-title"><h3>采购员账号</h3></div>
        <p class="paragraph">维护采购员账号、启停状态、角色权限和所属组织。</p>
        <button class="btn primary">进入管理</button>
      </div>
      <div class="card clickable-card" role="button" tabindex="0" onclick="setPage('admin-field-settings')">
        <div class="card-title"><h3>字段设置</h3></div>
        <p class="paragraph">管理 PR、供应商、证件、联系人等业务对象字段库。</p>
        <button class="btn primary">进入设置</button>
      </div>
      <div class="card clickable-card" role="button" tabindex="0" onclick="setPage('admin-form-settings')">
        <div class="card-title"><h3>表单设置</h3></div>
        <p class="paragraph">配置表单字段、分组、必填、显示条件和校验规则。</p>
        <button class="btn primary">进入配置</button>
      </div>
    </div>
  `;
}

function reportNav(active) {
  const items = [
    ["admin-report-executive", "经营总览"],
    ["admin-report-spend", "支出分析"],
    ["admin-report-process", "流程效率"],
    ["admin-report-suppliers", "供应商分析"],
    ["admin-report-risks", "异常与风险"],
  ];
  return `<div class="tabs report-tabs">${items.map(([page, label]) => `<button class="tab-btn ${active === page ? "active" : ""}" onclick="setPage('${page}')">${label}</button>`).join("")}</div>`;
}

function reportFilters(extra = []) {
  return filters([
    { label: "时间范围", options: ["本月", "本季度", "本年", "自定义"] },
    "部门",
    { label: "金额口径", options: ["合同金额", "PO金额", "已付款金额", "核价金额", "PR预算"] },
    ...extra,
  ]);
}

function barList(items, page = "") {
  const max = Math.max(...items.map((item) => item[1]));
  return `<div class="bar-list">${items.map(([label, value, desc, color]) => {
    const width = Math.round((value / max) * 100);
    const action = page ? ` onclick="setPage('${page}')" role="button" tabindex="0"` : "";
    return `<div class="bar-row"${action}>
      <div class="bar-row-top"><strong>${label}</strong><span>${desc}</span></div>
      <div class="bar-track"><span class="${color || ""}" style="width:${width}%"></span></div>
    </div>`;
  }).join("")}</div>`;
}

function trendBars(items) {
  const max = Math.max(...items.map((item) => item[1]));
  return `<div class="trend-bars">${items.map(([label, value]) => {
    const height = Math.max(18, Math.round((value / max) * 150));
    return `<div class="trend-bar"><div class="trend-col" style="height:${height}px"></div><strong>${label}</strong><span>${value}</span></div>`;
  }).join("")}</div>`;
}

function progressLine(label, value, desc, type = "") {
  return `<div class="progress-line-card">
    <div class="bar-row-top"><strong>${label}</strong><span>${value}%</span></div>
    <div class="bar-track"><span class="${type}" style="width:${value}%"></span></div>
    <div class="hint">${desc}</div>
  </div>`;
}

const reportSpendRows = [
  ["PR-202605-008", "RFQ-202605-008", "-", "-", "运营部", "仓储标签打印设备", "南京云杉办公用品有限公司", "¥72,000", "¥72,000", "¥0", "¥0", "¥0", "¥0", "0%", "待建合同", "未生成", "未付款"],
  ["PR-202605-010", "RFQ-202605-010", "CON-202605-002", "-", "行政部", "办公室保洁外包", "上海办公伙伴有限公司", "¥120,000", "¥120,000", "¥120,000", "¥0", "¥0", "¥0", "0%", "Worklife BPM审批中", "未生成", "未付款"],
  ["PR-202605-016", "RFQ-202605-016", "CON-202605-008", "PO-202605-003", "运营部", "仓库打包耗材", "山西德利仁信息技术服务有限公司", "¥48,000", "¥48,000", "¥48,000", "¥48,000", "¥0", "¥0", "0%", "合同完成", "同步失败", "未付款"],
  ["PR-202605-018", "RFQ-202605-018", "CON-202605-010", "PO-202605-005", "品牌部", "品牌视觉设计服务", "北京远景会务服务有限公司", "¥110,000", "¥110,000", "¥110,000", "¥110,000", "¥0", "¥0", "0%", "合同完成", "已同步", "付款中"],
  ["PR-202605-013", "RFQ-202605-013", "CON-202605-005", "PO-202605-007", "行政部", "会议室设备采购", "南京云杉办公用品有限公司", "¥85,000", "¥85,000", "¥85,000", "¥85,000", "¥85,000", "¥0", "0%", "合同完成", "已同步", "付款完成"],
];

const reportRiskRows = [
  ["高", "PO同步失败", "PO-202605-003", "审计辅助服务", "财务部", "上海博夷信息技术有限公司", "张伟", "¥180,000", "同步失败", "2天", "补充供应商税号后重试", "po-detail"],
  ["高", "付款失败", "PO-202605-008", "候选人测评服务", "人力部", "上海博夷信息技术有限公司", "张伟", "¥38,000", "付款失败", "1天", "联系财务核对失败原因", "admin-pos"],
  ["中", "待建合同超期", "RFQ-202605-008", "仓储标签打印设备", "运营部", "南京云杉办公用品有限公司", "张伟", "¥72,000", "待建合同", "3天", "尽快创建合同申请", "admin-contracts"],
  ["中", "证件即将到期", "VD 0003", "供应商证件", "行政部", "厦门玉树陶朱文化发展有限公司", "李娜", "-", "即将到期", "15天", "通知供应商更新证件", "suppliers"],
  ["低", "审批临近SLA", "RFQ-202605-006", "客户答谢会物料", "销售部", "山西德利仁信息技术服务有限公司", "张伟", "¥88,000", "审批中", "20小时", "采购经理确认审批", "admin-approval"],
];

function adminReportExecutivePage() {
  return `
    ${pageHead("采购分析 · 经营总览", "面向高管和采购经理查看采购支出、预算占用、节省效果和风险概览。", `<button class="btn">导出看板</button>`)}
    ${reportNav("admin-report-executive")}
    ${reportFilters([{ label: "采购策略", options: stateOptions.strategy }, "供应商"])}
    ${metrics([
      ["总采购预算", "¥1,245,000", "PR 预算合计", "purchase-requests"],
      ["核价总额", "¥1,086,800", "中标核价明细含税金额", "admin-rfqs"],
      ["预计节省", "¥158,200", "节省率 12.7%", "admin-rfqs"],
      ["合同金额", "¥986,000", "合同处理中/已完成", "admin-contracts"],
      ["已付款金额", "¥260,000", "付款完成率 35.0%", "admin-pos"],
    ])}
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>月度采购支出趋势</h3><span class="hint">金额口径：合同金额</span></div>
        ${trendBars([["1月", 82], ["2月", 96], ["3月", 110], ["4月", 138], ["5月", 124], ["6月", 152]])}
      </div>
      <div class="card">
        <div class="card-title"><h3>预算与付款进度</h3></div>
        ${progressLine("预算占用率", 87, "核价总额 / PR预算", "blue")}
        ${progressLine("付款完成率", 35, "已付款金额 / PO金额", "green")}
        ${progressLine("风险事项处理率", 62, "已关闭风险 / 全部风险", "orange")}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-3">
      <div class="card">
        <div class="card-title"><h3>部门支出排行</h3></div>
        ${barList([["IT部", 280, "¥280,000"], ["市场部", 150, "¥150,000"], ["财务部", 180, "¥180,000"], ["行政部", 120, "¥120,000"], ["人力部", 96, "¥96,000"]], "admin-report-spend")}
      </div>
      <div class="card">
        <div class="card-title"><h3>供应商支出排行</h3></div>
        ${barList([["上海博夷", 180, "¥180,000"], ["上海办公伙伴", 165, "¥165,000"], ["北京远景", 96, "¥96,000"], ["南京云杉", 72, "¥72,000"], ["山西德利仁", 53, "¥52,500"]], "admin-report-suppliers")}
      </div>
      <div class="card">
        <div class="card-title"><h3>高风险事项</h3></div>
        ${table(["等级", "风险", "单据", "操作"], reportRiskRows.slice(0, 4).map((r) => [tag(r[0], r[0] === "高" ? "red" : "orange"), r[1], r[2], `<button class="btn link" onclick="setPage('${r[11]}')">处理</button>`]))}
      </div>
    </div>
  `;
}

function adminReportSpendPage() {
  const f = currentFilters();
  const rows = reportSpendRows.filter((r) =>
    textIncludes([r[4]], f.f1) &&
    textIncludes([r[6], r[5], r[0], r[1], r[2], r[3]], f.f3) &&
    exactMatch(r[16], f.f4)
  );
  return `
    ${pageHead("采购分析 · 支出分析", "按部门、供应商、采购类型和金额口径分析采购支出结构。", `<button class="btn">导出明细</button>`)}
    ${reportNav("admin-report-spend")}
    ${reportFilters(["供应商 / 项目 / 单据", { label: "付款状态", options: stateOptions.payment }])}
    <div class="grid cols-3">
      <div class="card">
        <div class="card-title"><h3>金额口径对比</h3></div>
        ${barList([["PR预算", 1245, "¥1,245,000"], ["核价金额", 1087, "¥1,086,800"], ["合同金额", 986, "¥986,000"], ["PO金额", 743, "¥742,914"], ["已付款", 260, "¥260,000"]])}
      </div>
      <div class="card">
        <div class="card-title"><h3>采购类型占比</h3></div>
        ${barList([["询比价", 68, "68%"], ["定向采购", 12, "12%", "orange"], ["单一来源", 10, "10%", "purple"], ["续约", 10, "10%", "green"]])}
      </div>
      <div class="card">
        <div class="card-title"><h3>节省表现</h3></div>
        ${infoGrid([
          ["预计节省", "¥158,200"],
          ["平均节省率", "12.7%"],
          ["非最低价中标", "2 单需说明理由"],
        ])}
      </div>
    </div>
    <div style="height:16px"></div>
    ${tableWrap(["PR号", "RFQ", "合同", "PO", "部门", "项目", "供应商", "PR预算", "核价金额", "合同金额", "PO金额", "已付款", "节省率", "合同", "PO", "付款", "操作"], rows.map((r) => [
      r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10], r[11], r[13], tag(r[14]), tag(r[15]), tag(r[16]), `<button class="btn link" onclick="setPage('purchase-requests')">下钻</button>`,
    ]))}
  `;
}

function adminReportProcessPage() {
  return `
    ${pageHead("采购分析 · 流程效率", "查看 PR、RFQ、核价、合同、PO、付款各阶段耗时和堵点。", `<button class="btn">导出耗时明细</button>`)}
    ${reportNav("admin-report-process")}
    ${reportFilters([{ label: "采购员", options: ["张伟", "李娜", "全部"] }, { label: "RFQ状态", options: stateOptions.rfq }])}
    ${metrics([
      ["PR处理平均耗时", "1.6天", "创建RFQ - PR接收"],
      ["核价平均耗时", "2.4天", "提交审批 - 开标"],
      ["审批平均耗时", "0.8天", "审批完成 - 提交审批"],
      ["合同完成平均耗时", "4.3天", "合同完成 - 创建合同"],
      ["付款平均耗时", "8.2天", "付款完成 - PO同步"],
    ])}
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>采购流程漏斗</h3><span class="hint">点击异常阶段进入对应列表</span></div>
      <div class="report-funnel">
        ${[
          ["PR接收", "42", "超时 3"],
          ["创建RFQ", "36", "未处理 6"],
          ["报价完成", "31", "未报价 8"],
          ["核价提交", "22", "待核价 5"],
          ["审批通过", "19", "驳回 2"],
          ["合同完成", "14", "处理中 5"],
          ["PO已同步", "12", "失败 1"],
          ["付款完成", "7", "待付 5"],
        ].map(([name, count, hint]) => `<button class="funnel-step" onclick="setPage('admin-report-risks')"><strong>${count}</strong><span>${name}</span><em>${hint}</em></button>`).join("")}
      </div>
    </div>
    <div style="height:16px"></div>
    ${tableWrap(["采购员", "处理PR数", "创建RFQ数", "完成核价数", "平均核价耗时", "审批通过率", "合同完成数", "PO同步失败", "付款异常"], [
      ["张伟", "24", "18", "11", "2.1天", "92%", "8", "1", "1"],
      ["李娜", "18", "14", "8", "2.9天", "86%", "6", "0", "1"],
      ["王强", "9", "7", "5", "1.8天", "100%", "4", "0", "0"],
    ])}
  `;
}

function adminReportSuppliersPage() {
  return `
    ${pageHead("采购分析 · 供应商分析", "查看供应商支出贡献、响应表现、中标率和证件风险。", `<button class="btn">导出供应商分析</button>`)}
    ${reportNav("admin-report-suppliers")}
    ${reportFilters([{ label: "供应商状态", options: stateOptions.supplier }, { label: "证件状态", options: stateOptions.certExpiry }])}
    ${metrics([
      ["合作中供应商", "32", "可参与询价", "suppliers"],
      ["有交易供应商", "18", "近 90 天有报价/合同"],
      ["Top10支出占比", "64%", "供应商集中度"],
      ["平均响应率", "82%", "已报价 / 被邀请"],
      ["证件风险", "6", "即将到期/已过期", "suppliers"],
    ])}
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>供应商支出与中标贡献</h3></div>
        ${barList([["上海博夷信息技术有限公司", 180, "中标 3 次 · ¥180,000"], ["上海办公伙伴有限公司", 165, "中标 2 次 · ¥165,000"], ["北京远景会务服务有限公司", 96, "中标 1 次 · ¥96,000"], ["南京云杉办公用品有限公司", 72, "中标 1 次 · ¥72,000"], ["山西德利仁信息技术服务有限公司", 53, "中标 1 次 · ¥52,500"]], "suppliers")}
      </div>
      <div class="card">
        <div class="card-title"><h3>供应商风险分布</h3></div>
        ${barList([["证件即将到期", 4, "4 家", "orange"], ["证件已过期", 2, "2 家", "red"], ["报价响应低", 5, "5 家", "purple"], ["单一供应商依赖", 2, "2 家", "blue"], ["付款异常", 1, "1 家", "red"]])}
      </div>
    </div>
    <div style="height:16px"></div>
    ${tableWrap(["供应商ID", "供应商名称", "状态", "参与RFQ", "报价次数", "中标次数", "中标率", "合同金额", "PO金额", "已付款", "响应率", "证件状态", "操作"], [
      ["VD 0001", "上海博夷信息技术有限公司", tag("合作中"), "8", "7", "3", "42.9%", "¥180,000", "¥180,000", "¥0", "87.5%", tag("正常"), `<button class="btn link" onclick="setPage('supplier-detail')">详情</button>`],
      ["VD 0002", "山西德利仁信息技术服务有限公司", tag("待审核信息"), "5", "5", "1", "20.0%", "¥52,500", "¥70,914", "¥0", "100%", tag("正常"), `<button class="btn link" onclick="setPage('supplier-detail')">详情</button>`],
      ["VD 0003", "厦门玉树陶朱文化发展有限公司", tag("待完善信息"), "3", "2", "0", "0%", "¥36,000", "¥0", "¥0", "66.7%", tag("即将到期"), `<button class="btn link" onclick="setPage('supplier-detail')">详情</button>`],
      ["VD 0004", "上海乐尔芙农业科技有限公司", tag("合作中"), "2", "1", "0", "0%", "¥0", "¥0", "¥0", "50.0%", tag("已过期"), `<button class="btn link" onclick="setPage('supplier-detail')">详情</button>`],
    ])}
  `;
}

function adminReportRisksPage() {
  const f = currentFilters();
  const rows = reportRiskRows.filter((r) =>
    textIncludes([r[4]], f.f1) &&
    textIncludes([r[1], r[2], r[3], r[5], r[6]], f.f3) &&
    exactMatch(r[0], f.f4)
  );
  return `
    ${pageHead("采购分析 · 异常与风险", "集中查看超预算、超期、PO同步失败、付款异常和证件风险。", `<button class="btn">导出风险清单</button>`)}
    ${reportNav("admin-report-risks")}
    ${reportFilters(["风险 / 单据 / 供应商", { label: "风险等级", options: ["高", "中", "低"] }])}
    ${metrics([
      ["高风险", "2", "需当天处理"],
      ["中风险", "2", "需本周处理"],
      ["低风险", "1", "持续跟踪"],
      ["PO同步失败", "1", "接口或主数据异常", "admin-pos"],
      ["付款异常", "1", "台账回传失败", "admin-pos"],
    ])}
    <div style="height:16px"></div>
    ${tableWrap(["风险等级", "风险类型", "关联单据", "项目", "部门", "供应商", "采购员", "金额", "当前状态", "停留时长", "建议动作", "操作"], rows.map((r) => [
      tag(r[0], r[0] === "高" ? "red" : r[0] === "中" ? "orange" : "gray"), r[1], r[2], r[3], r[4], r[5], r[6], r[7], tag(r[8]), r[9], r[10], `<button class="btn link" onclick="setPage('${r[11]}')">处理</button>`,
    ]))}
  `;
}

function adminListPage(title) {
  return `
    ${pageHead(title, "管理基础数据与系统记录。")}
    ${tableWrap(["名称", "状态", "创建时间", "操作"], [
      ["张伟 / 13800010001 / zhangwei@company.com", tag("启用", "green"), "2026-04-01", `<button class="btn link">编辑</button>`],
      ["李娜 / 13800010002 / lina@company.com", tag("启用", "green"), "2026-04-01", `<button class="btn link">编辑</button>`],
      ["营业执照", tag("启用", "green"), "2026-04-01", `<button class="btn link">编辑</button>`],
      ["完税证明", tag("启用", "green"), "2026-04-01", `<button class="btn link">编辑</button>`],
    ])}
  `;
}

function fieldSettingsPage() {
  const rows = Object.entries(fieldLibraries).map(([key, library]) => [
    library.name,
    library.object,
    String(library.fields.length),
    library.updated,
    `<button class="btn link" onclick="selectFieldLibrary('${key}')">查看字段</button>`,
  ]);
  return `
    <div class="card">
      <div class="card-title"><h3>表单字段库</h3><span class="hint">先选择表单，再维护字段</span></div>
      ${table(["字段库", "业务对象", "字段数", "最近更新", "操作"], rows)}
    </div>
  `;
}

function fieldLibraryDetailPage() {
  const library = fieldLibraries[state.selectedFieldLibrary] || fieldLibraries.certificate;
  const fieldRows = library.fields.map((item) => [
    item[0],
    item[1],
    item[2],
    item[3],
    item[4],
    item[5],
    tag("启用", "green"),
    `<button class="btn link" onclick="setPage('admin-field-detail')">编辑</button>`,
  ]);
  const isCertificate = state.selectedFieldLibrary === "certificate";
  return `
    ${pageHead("字段库详情", `${library.name}。${library.desc}`, `
      <button class="btn" onclick="setPage('admin-field-settings')">返回字段库列表</button>
      <button class="btn primary" onclick="setPage('admin-field-create')">新增字段</button>
    `)}
    <div class="grid cols-1">
      <div class="card">
        <div class="card-title"><h3>${library.name}</h3><span class="hint">字段只归属当前表单</span></div>
        ${table(["字段编码", "字段名称", "控件类型", "数据类型", "校验规则", "字段分组", "状态", "操作"], fieldRows)}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>${isCertificate ? "证件类型字段绑定" : "字段分组"}</h3>${isCertificate ? `<button class="btn" onclick="openModal('certTypeFieldConfig')">配置证件字段</button>` : ""}</div>
        ${isCertificate ? table(["证件类型", "基础字段", "扩展字段", "供应商可见", "状态"], [
          ["营业执照", "证件类型、证件文件、有效期开始、有效期截止", "统一社会信用代码、注册地址", tag("是", "green"), tag("启用", "green")],
          ["完税证明", "证件类型、证件文件、有效期开始、有效期截止", "纳税期间、税务机关", tag("是", "green"), tag("启用", "green")],
          ["食品经营许可证", "证件类型、证件文件、有效期开始、有效期截止", "许可证编号、经营项目、发证机关", tag("是", "green"), tag("启用", "green")],
          ["医疗机构执业许可证", "证件类型、证件文件、有效期开始、有效期截止", "机构登记号、诊疗科目", tag("是", "green"), tag("启用", "green")],
        ]) : table(["字段分组", "示例字段", "业务用途"], groupedFieldSummary(library.fields))}
      </div>
      <div class="card">
        <div class="card-title"><h3>字段使用关系</h3></div>
        <div class="timeline">
          ${timeline("当前字段库", `${library.name} / ${library.object}`)}
          ${timeline("字段维护", "字段编码在所属表单内唯一，字段变更只影响当前表单")}
          ${timeline("表单呈现", "表单设置页面使用当前字段库中的字段进行排序、分组和条件显示")}
        </div>
      </div>
    </div>
  `;
}

function fieldCreatePage() {
  const library = fieldLibraries[state.selectedFieldLibrary] || fieldLibraries.certificate;
  return `
    ${pageHead("创建字段", "在指定表单字段库中创建字段。字段仅归属当前表单，不跨表单复用。", `
      <button class="btn" onclick="setPage('admin-field-settings')">返回字段设置</button>
      <button class="btn primary" onclick="setPage('admin-field-detail')">保存字段</button>
    `)}
    ${steps(["选择表单字段库", "字段基础信息", "控件与校验", "保存生效"], 2)}
    <div style="height:16px"></div>
    <div class="grid cols-3">
      <div class="card" style="grid-column: span 2;">
        <div class="card-title"><h3>字段基础信息</h3><span class="hint">字段编码在所属表单字段库内唯一</span></div>
        <div class="form-grid">
          ${field("所属表单字段库", library.name, true)}
          ${field("字段编码", "certificate_license_no")}
          ${field("字段名称", "许可证编号")}
          ${selectField("控件类型", ["文本", "多行文本", "数字", "金额", "日期", "下拉选择", "附件上传"])}
          ${selectField("数据类型", ["STRING", "DECIMAL", "DATE", "FILE", "BOOLEAN"])}
          ${selectField("默认是否必填", ["是", "否"])}
          ${selectField("是否敏感字段", ["否", "是，列表和导出脱敏"])}
          ${field("字段说明", "用于食品经营许可证等证件的许可证编号录入")}
          ${field("占位提示", "请输入许可证编号")}
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>保存后流转</h3></div>
        <div class="timeline">
          ${timeline("1. 进入所属字段库", `字段以启用状态进入${library.name}`)}
          ${timeline("2. 配置当前表单", "管理员在表单设置中对当前表单字段排序、分组和配置条件显示")}
          ${timeline("3. 业务页面生效", `${library.object}业务页面按当前表单配置渲染字段`)}
        </div>
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>校验规则</h3></div>
        <div class="form-grid">
          ${selectField("格式校验", ["无", "统一社会信用代码", "手机号", "邮箱", "金额大于0", "自定义正则"])}
          ${field("最大长度", "50")}
          ${field("文件类型", "适用于附件上传：PDF,JPG,PNG")}
          ${field("文件大小上限", "适用于附件上传：10MB")}
        </div>
        <div class="notice" style="margin-top:14px;">表单设置可在所属表单字段默认校验基础上做覆盖，但不能放宽系统级安全校验，例如文件大小和敏感字段脱敏。</div>
      </div>
      <div class="card">
        <div class="card-title"><h3>所属字段库</h3></div>
        ${table(["表单字段库", "业务对象", "是否当前字段库"], Object.values(fieldLibraries).map((item) => [
          item.name,
          item.object,
          item.name === library.name ? tag("是", "green") : tag("否", "gray"),
        ]))}
      </div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('admin-field-settings')">取消</button>
      <button class="btn" onclick="showToast('已保存并继续在当前表单字段库新增')">保存并继续新增</button>
      <button class="btn primary" onclick="setPage('admin-field-detail')">保存字段</button>
    </div>
  `;
}

function fieldDetailPage() {
  const library = fieldLibraries[state.selectedFieldLibrary] || fieldLibraries.certificate;
  const sample = state.selectedFieldLibrary === "certificate"
    ? library.fields.find((item) => item[0] === "certificate_license_no") || library.fields[0]
    : library.fields[0];
  const libraryRows = Object.values(fieldLibraries).map((item) => [
    item.name,
    item.object,
    item.name === library.name ? tag("是", "green") : tag("否", "gray"),
  ]);
  return `
    ${pageHead("字段详情", "编辑当前表单字段库中的字段。保存后只影响所属表单。", `
      <button class="btn" onclick="setPage('admin-field-settings')">返回字段设置</button>
      <button class="btn primary" onclick="showToast('字段已保存，${library.name}将读取最新配置')">保存修改</button>
    `)}
    <div class="detail-hero">
      <div>
        <div class="hero-kicker">所属字段库：${library.name} / 字段编码：${sample[0]}</div>
        <h2>${sample[1]}</h2>
        <p>控件类型：${sample[2]} · 数据类型：${sample[3]} · 字段分组：${sample[5]} · 仅用于当前表单</p>
      </div>
      <div class="hero-status">${tag("启用", "green")}</div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>字段定义</h3><span class="hint">字段编码已被当前表单使用，不建议修改</span></div>
        <div class="form-grid">
          ${field("所属表单字段库", library.name, true)}
          ${field("字段编码", sample[0], true)}
          ${field("字段名称", sample[1])}
          ${selectField("控件类型", ["文本", "多行文本", "数字", "金额", "日期", "下拉选择", "附件上传"])}
          ${selectField("数据类型", ["STRING", "DECIMAL", "DATE", "FILE", "BOOLEAN"])}
          ${selectField("默认是否必填", ["是", "否"])}
          ${selectField("是否敏感字段", ["否", "是，列表和导出脱敏"])}
          ${field("占位提示", `请输入${sample[1]}`)}
          ${field("字段说明", `${library.desc} / ${sample[4]}`)}
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>校验与状态</h3></div>
        <div class="form-grid">
          ${selectField("字段状态", ["启用", "停用"])}
          ${selectField("格式校验", ["最大长度", "无", "统一社会信用代码", "手机号", "邮箱", "金额大于0", "自定义正则"])}
          ${field("最大长度", "50")}
          ${field("自定义正则", "")}
          ${field("选项来源", "不适用")}
          ${field("错误提示", "请输入正确的许可证编号")}
        </div>
        <div class="notice" style="margin-top:14px;">当前表单已有历史提交记录时不建议修改字段编码。若停用字段，历史提交记录保留字段快照，业务页面不再展示该字段。</div>
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>所属字段库</h3></div>
        ${table(["表单字段库", "业务对象", "是否可编辑当前字段"], libraryRows)}
      </div>
      <div class="card">
        <div class="card-title"><h3>表单内使用位置</h3><button class="btn" onclick="setPage('admin-form-config')">进入表单配置</button></div>
        ${table(["使用表单", "分组", "显示条件", "使用页面"], [
          [library.name.replace("字段库", ""), sample[5], state.selectedFieldLibrary === "certificate" ? "按证件类型动态显示" : "始终显示", `${library.object}业务页面`],
          [library.name.replace("字段库", ""), sample[5], "详情页/审核页读取字段快照", "详情、审核、报表取数"],
        ])}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>变更影响</h3></div>
      <div class="timeline">
        ${timeline("表单渲染", `保存后，${library.name.replace("字段库", "")}会读取最新字段名称、控件提示和默认校验`)}
        ${timeline("历史记录", "历史提交记录保留提交时字段快照，不被本次变更覆盖")}
        ${timeline("删除限制", "当前表单已有历史提交记录时不可物理删除字段，只能停用或复制后调整")}
      </div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('admin-field-settings')">返回字段设置</button>
      <button class="btn" onclick="showToast('已保存字段修改')">保存</button>
      <button class="btn primary" onclick="showToast('字段已保存，所属表单将读取最新配置')">保存并生效</button>
    </div>
  `;
}

function formSettingsPage() {
  const rows = Object.entries(fieldLibraries).map(([key, library]) => {
    const meta = formTemplateMeta[key];
    return [
      meta.name,
      library.object,
      meta.useSide,
      String(library.fields.length),
      library.updated,
      `<button class="btn link" onclick="selectFormTemplate('${key}')">查看配置</button>`,
    ];
  });
  return `
    <div class="card">
      <div class="card-title"><h3>表单列表</h3><span class="hint">先选择表单，再维护配置</span></div>
      ${table(["表单名称", "业务对象", "使用端", "字段数", "最近更新", "操作"], rows)}
    </div>
  `;
}

function formDetailPage() {
  const key = state.selectedFormTemplate || "certificate";
  const library = fieldLibraries[key] || fieldLibraries.certificate;
  const meta = formTemplateMeta[key] || formTemplateMeta.certificate;
  const fieldRows = library.fields.slice(0, 8).map((item, index) => [
    String(index + 1),
    item[5],
    item[1],
    item[2],
    index < 4 ? tag("是", "green") : tag("否", "gray"),
    key === "certificate" && index >= 4 ? "按证件类型显示" : "始终显示",
  ]);
  return `
    ${pageHead("表单详情", `${meta.name}。表单配置、字段排序、预览和流转说明都在具体表单内维护。`, `
      <button class="btn" onclick="setPage('admin-form-settings')">返回表单列表</button>
      <button class="btn" onclick="setPage('admin-form-preview')">预览</button>
      <button class="btn primary" onclick="setPage('admin-form-config')">配置字段</button>
    `)}
    <div class="card">
      <div class="card-title"><h3>当前表单字段</h3><span class="hint">${library.name}</span></div>
      ${table(["排序", "分组", "字段", "控件", "必填", "显示条件"], fieldRows)}
    </div>
  `;
}

function formCreatePage() {
  return `
    ${pageHead("创建表单", "选择业务对象并创建表单，系统同步生成该表单独立字段库。", `
      <button class="btn" onclick="setPage('admin-form-settings')">返回表单列表</button>
      <button class="btn primary" onclick="setPage('admin-form-config')">下一步：配置字段</button>
    `)}
    ${steps(["表单基础信息", "配置字段", "预览验证", "保存生效"], 1)}
    <div style="height:16px"></div>
    <div class="grid cols-3">
      <div class="card" style="grid-column: span 2;">
        <div class="card-title"><h3>表单基础信息</h3></div>
        <div class="form-grid">
          ${field("表单名称", "供应商证件上传表单")}
          ${selectField("业务对象", ["Certificate 供应商证件", "Supplier 供应商企业信息", "PurchaseRequest 采购申请单", "Contact 联系人"])}
          ${selectField("使用端", ["供应商端 + 采购员端", "采购员端", "采购经理端", "供应商端"])}
          ${field("表单说明", "用于供应商或采购员上传证件，并按证件类型动态展示扩展字段")}
          ${selectField("数据保存方式", ["主表字段 + 扩展字段", "仅主表字段", "仅扩展字段"])}
          ${selectField("历史快照", ["提交时保存字段快照", "不保存快照"])}
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>创建后流程</h3></div>
        <div class="timeline">
          ${timeline("1. 创建表单", "确定业务对象和使用端")}
          ${timeline("2. 配置字段", "在该表单自己的字段库中创建字段，并设置分组、排序、必填和显示条件")}
          ${timeline("3. 预览验证", "模拟业务页面填写，检查校验和动态显示")}
          ${timeline("4. 保存生效", "不区分发布/草稿，保存后当前配置直接生效")}
        </div>
      </div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('admin-form-settings')">取消</button>
      <button class="btn primary" onclick="setPage('admin-form-config')">下一步：配置字段</button>
    </div>
  `;
}

function formConfigPage() {
  const key = state.selectedFormTemplate || "certificate";
  const library = fieldLibraries[key] || fieldLibraries.certificate;
  const meta = formTemplateMeta[key] || formTemplateMeta.certificate;
  const allRows = library.fields.map((item, index) => [
    item[1],
    item[0],
    item[2],
    item[4],
    index < 5 ? tag("已在表单中", "green") : tag("可配置", "orange"),
    `<button class="btn link" onclick="setPage('admin-field-detail')">编辑字段</button>`,
  ]);
  const configuredRows = library.fields.slice(0, 6).map((item, index) => [
    String(index + 1),
    item[5],
    item[1],
    index < 4 ? tag("是", "green") : tag("否", "gray"),
    key === "certificate" && index >= 4 ? "按证件类型显示" : "始终显示",
    `<button class="btn link">上移</button><button class="btn link">移除</button>`,
  ]);
  return `
    ${pageHead("配置表单字段", `维护${meta.name}的字段库、字段顺序、分组、必填、只读、默认值和显示条件。`, `
      <button class="btn" onclick="setPage('admin-form-detail')">返回表单详情</button>
      <button class="btn primary" onclick="setPage('admin-form-preview')">预览表单</button>
    `)}
    ${steps(["表单基础信息", "配置字段", "预览验证", "保存生效"], 2)}
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>当前表单字段库</h3><button class="btn" onclick="setPage('admin-field-create')">新增字段</button></div>
        ${table(["字段名称", "字段编码", "控件", "默认校验", "状态", "操作"], allRows)}
      </div>
      <div class="card">
        <div class="card-title"><h3>当前表单字段</h3><span class="hint">${meta.name}</span></div>
        ${table(["排序", "分组", "字段", "必填", "显示条件", "操作"], configuredRows)}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>字段覆盖规则</h3></div>
        <div class="form-grid">
          ${selectField("当前字段", library.fields.slice(0, 6).map((item) => item[1]))}
          ${selectField("必填覆盖", ["跟随字段默认", "本表单必填", "本表单非必填"])}
          ${field("显示条件", key === "certificate" ? "certificate_type = 食品经营许可证" : "始终显示")}
          ${field("错误提示", `请输入正确的${library.fields[0][1]}`)}
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>保存说明</h3></div>
        <div class="notice">表单配置不区分发布或草稿。点击保存后，${meta.name}业务页面会读取当前配置；历史记录仍保留提交时字段快照。</div>
        <div class="timeline" style="margin-top:14px;">
          ${timeline("读取当前表单字段库", `字段名称、控件类型和默认校验只来自${library.name}`)}
          ${timeline("读取表单规则", "分组、排序、必填覆盖和显示条件来自表单设置")}
          ${timeline("业务提交", "提交后保存主表字段、扩展字段值和字段快照")}
        </div>
      </div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('admin-form-detail')">返回详情</button>
      <button class="btn" onclick="showToast('当前字段配置已保存')">保存当前配置</button>
      <button class="btn primary" onclick="setPage('admin-form-preview')">预览表单</button>
    </div>
  `;
}

function formPreviewPage() {
  const key = state.selectedFormTemplate || "certificate";
  const library = fieldLibraries[key] || fieldLibraries.certificate;
  const meta = formTemplateMeta[key] || formTemplateMeta.certificate;
  const previewValues = {
    pr: ["PR-202605-001", "普通PR", "王芳", "市场部", "线下活动礼品采购", "市场物料"],
    supplier: ["VD 0001", "上海博夷信息技术有限公司", "沈家伟", "2016-01-07", "5000000", "上海市奉贤区奉浦大道1599号"],
    certificate: ["食品经营许可证", "食品经营许可证_上海博夷.pdf", "2024-01-01", "2029-12-31", "JY13101200000000", "上海市市场监督管理局"],
    contact: ["沈家伟", "13321832330", "shen@example.com", "业务经理", "业务部"],
  };
  const values = previewValues[key] || previewValues.certificate;
  const previewFields = library.fields.slice(0, 6).map((item, index) => {
    const value = values[index] || item[1];
    if (item[2].includes("下拉") || item[2] === "SELECT") return selectField(item[1], [value, "选项二", "选项三"]);
    return field(item[1], value);
  }).join("");
  return `
    ${pageHead("表单预览", `模拟${meta.name}读取当前表单配置后的填写效果。`, `
      <button class="btn" onclick="setPage('admin-form-config')">返回配置字段</button>
      <button class="btn primary" onclick="showToast('${meta.previewAction}')">查看业务页面</button>
    `)}
    ${steps(["表单基础信息", "配置字段", "预览验证", "保存生效"], 3)}
    <div style="height:16px"></div>
    <div class="grid cols-3">
      <div class="card" style="grid-column: span 2;">
        <div class="card-title"><h3>预览：${meta.name}</h3><span class="hint">${library.object}</span></div>
        <div class="form-grid">
          ${previewFields}
        </div>
        <div class="notice" style="margin-top:14px;">预览内容来自${library.name}。保存表单配置后，业务页面会读取当前字段顺序、分组、必填和显示条件。</div>
      </div>
      <div class="card">
        <div class="card-title"><h3>校验结果</h3></div>
        ${table(["校验项", "结果", "说明"], [
          ["必填字段", tag("通过", "green"), "当前表单必填字段均已填写"],
          ["格式规则", tag("通过", "green"), "字段格式符合当前表单字段库校验规则"],
          ["条件显示", tag("通过", "green"), key === "certificate" ? "证件类型扩展字段正常显示" : "当前字段均按配置显示"],
        ])}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>业务闭环</h3></div>
      <div class="timeline">
        ${timeline("表单设置保存", "管理员保存当前字段组合、顺序和显示条件")}
        ${timeline("业务页面读取", `${meta.name}业务页面读取当前表单配置`)}
        ${timeline("提交校验", "提交时执行必填、格式、附件和条件显示校验")}
        ${timeline("结构化沉淀", "字段值和字段快照保存到业务记录，用于详情、审核和报表")}
      </div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('admin-form-config')">返回配置</button>
      <button class="btn" onclick="setPage('admin-form-settings')">完成并回列表</button>
      <button class="btn primary" onclick="showToast('${meta.previewAction}')">查看业务页面</button>
    </div>
  `;
}

function adminRfqQueryPage() {
  const f = currentFilters();
  const rows = rfqs.map((r, i) => ({ r, i })).filter(({ r, i }) => {
    const status = i === 0 ? state.rfqStatus : r[5];
    return textIncludes([r[0], r[1], r[2]], f.f0) &&
      exactMatch(status, f.f1) &&
      exactMatch(r[3], f.f2) &&
      exactMatch(r[4], f.f3);
  });
  return `
    ${pageHead("询价单查询", "采购经理查看全量询价单状态，页面仅提供查询与查看，不提供采购员操作。", `
      <button class="btn primary" onclick="setPage('create-rfq')">创建询价单</button>
    `)}
    ${filters([
      "项目名称 / PR号",
      { label: "RFQ状态", options: stateOptions.rfq },
      { label: "采购策略", options: stateOptions.strategy },
      { label: "报价方式", options: stateOptions.quoteMode },
    ])}
    ${tableWrap(["RFQ编号", "项目名称", "PR号", "采购员", "采购策略", "报价方式", "状态", "核价总额", "操作"], rows.map(({ r, i }) => [
      r[0],
      r[1],
      r[2],
      i === 2 ? "李娜" : "张伟",
      r[3],
      tag(r[4]),
      tag(i === 0 ? state.rfqStatus : r[5]),
      i === 0 ? "¥70,914" : i === 1 ? "¥280,000" : "¥58,600",
      `<button class="btn link" onclick="selectRfq('${r[0]}','rfq-detail')">查看</button>`,
    ]))}
  `;
}

function adminContractsPage() {
  const f = currentFilters();
  const rows = contracts.map((c, i) => ({ c, i })).filter(({ c, i }) => c[0] !== "-" && (() => {
    const buyer = i % 2 === 0 ? "张伟" : "李娜";
    return textIncludes([c[0], c[1], c[2], c[3]], f.f0) &&
      exactMatch(c[6], f.f1) &&
      textIncludes([c[4]], f.f2) &&
      textIncludes([buyer], f.f3);
  })());
  return `
    ${pageHead("合同管理", "采购经理查看合同申请、Worklife BPM 审批、签署归档、PO 发起和 PR 同步状态。")}
    ${metrics([["合同草稿", String(contracts.filter((c) => c[0] !== "-" && c[6] === "合同草稿").length)], ["外部审批推送中", String(contracts.filter((c) => c[6] === "外部审批推送中").length)], ["Worklife BPM审批中", String(contracts.filter((c) => c[6] === "Worklife BPM审批中").length)], ["合同完成", String(contracts.filter((c) => c[6] === "合同完成").length)]])}
    <div style="height:16px"></div>
    ${filters([
      "合同编号 / RFQ / PR",
      { label: "合同状态", options: stateOptions.contract },
      "供应商",
      "采购员",
    ])}
    ${tableWrap(["合同编号", "合同名称", "关联PR", "关联RFQ", "采购员", "中标供应商", "金额", "货币", "合同状态", "PO状态", "付款状态", "操作"], rows.map(({ c, i }) => [
      c[0],
      c[1],
      c[2],
      c[3],
      i % 2 === 0 ? "张伟" : "李娜",
      c[4],
      contractAmount(c[5]),
      contractCurrency(c[5]),
      tag(c[6]),
      tag(poApprovalStatus(relatedPoRowsForPr(c[2])[0])),
      tag(prPaymentStatus(c[2])),
      `<button class="btn link" onclick="selectContract('${c[0]}','${c[2]}','admin-contract-detail')">查看</button>`,
    ]))}
    <div class="notice" style="margin-top:14px;">采购经理端为合同管理查看和跟踪视图；合同创建由采购员在合同管理列表顶部「创建合同」发起。</div>
  `;
}

function contractsPage() {
  const f = currentFilters();
  const rows = contracts.map((c, i) => ({ c, i })).filter(({ c, i }) => c[0] !== "-" && (() => {
    return textIncludes([c[0], c[1], c[2], c[3]], f.f0) &&
      exactMatch(c[6], f.f1) &&
      textIncludes([c[4]], f.f2);
  })());
  return `
    ${pageHead("合同管理", "核价审批通过后，在列表顶部点击「创建合同」，选择已审批完成的采购询价单并填写合同数据生成合同申请。", `<button class="btn primary" onclick="setPage('contract-create')">创建合同</button>`)}
    <div class="notice" style="margin-bottom:14px;">合同创建统一从本页发起：先选择状态为「已完成」的采购询价单，再录入合同信息，生成合同申请并进入 Worklife BPM 审批。</div>
    ${metrics([["合同草稿", String(contracts.filter((c) => c[0] !== "-" && c[6] === "合同草稿").length)], ["外部审批推送中", String(contracts.filter((c) => c[6] === "外部审批推送中").length)], ["Worklife BPM审批中", String(contracts.filter((c) => c[6] === "Worklife BPM审批中").length)], ["待签署", String(contracts.filter((c) => c[6] === "待签署").length)], ["合同完成", String(contracts.filter((c) => c[6] === "合同完成").length)]])}
    <div style="height:16px"></div>
    ${filters([
      "合同编号 / RFQ / PR",
      { label: "合同状态", options: stateOptions.contract },
      "供应商",
      "创建时间",
    ])}
    ${tableWrap(["合同编号", "合同名称", "关联PR", "关联RFQ", "中标供应商", "金额", "货币", "合同状态", "PR同步", "PO状态", "付款状态", "操作"], rows.map(({ c, i }) => [
      c[0],
      c[1],
      c[2],
      c[3],
      c[4],
      contractAmount(c[5]),
      contractCurrency(c[5]),
      tag(c[6]),
      tag(c[7]),
      tag(poApprovalStatus(relatedPoRowsForPr(c[2])[0])),
      tag(prPaymentStatus(c[2])),
      `<button class="btn link" onclick="selectContract('${c[0]}','${c[2]}')">详情</button>`,
    ]))}
  `;
}

function poListPage() {
  const f = currentFilters();
  const dynamicRows = state.contractStatus === "合同完成"
    ? [["PO-202605-010", "PR-202605-001", "CON-202605-013", "RFQ-202605-001", "山西德利仁、上海博夷", "¥70,914", state.poStatus, state.paymentStatus, "演示流：合同归档后发起PO"]]
    : [];
  const sourceRows = [...dynamicRows, ...poRows];
  const rows = sourceRows.filter((po) =>
    textIncludes([po[0], po[1], po[2], po[3]], f.f0) &&
    exactMatch(po[7], f.f1) &&
    textIncludes([po[4]], f.f2)
  );
  return `
    ${pageHead("履约与付款", "合同签署归档后由业务人员发起 PO，采购端查看 PO 与付款状态。")}
    ${metrics([["审批通过", String(sourceRows.filter((po) => poApprovalStatus(po) === "审批通过").length)], ["付款中", String(sourceRows.filter((po) => po[7] === "付款中").length)], ["付款完成", String(sourceRows.filter((po) => po[7] === "付款完成").length)]])}
    <div style="height:16px"></div>
    ${filters([
      "PO / PR / 合同 / RFQ",
      { label: "付款状态", options: stateOptions.payment },
      "供应商",
    ])}
    ${tableWrap(["PO编号", "关联PR", "合同编号", "RFQ编号", "供应商", "PO金额", "货币", "PO状态", "付款状态", "操作"], rows.map((po) => [
      `<button class="btn link" onclick="selectPo('${po[0]}')">${po[0]}</button>`, po[1], po[2], po[3], po[4], po[5], poCurrency(po[5]), tag(poApprovalStatus(po)), tag(po[7]), `<button class="btn link" onclick="selectPo('${po[0]}')">详情</button>`,
    ]))}
  `;
}

function poDetailPage() {
  let po = findPo();
  const isDynamicPrimary = !po && state.selectedPr === "PR-202605-001";
  if (isDynamicPrimary) {
    po = ["PO-202605-010", "PR-202605-001", "CON-202605-013", "RFQ-202605-001", "山西德利仁、上海博夷", "¥70,914", state.poStatus, state.paymentStatus, "演示流：合同归档后发起PO"];
  }
  po = po || poRows[0];
  const poStatus = isDynamicPrimary ? state.poStatus : po[6];
  const ledgerStatus = poLedgerStatus(po, isDynamicPrimary);
  const approvalStatus = poApprovalStatus(po, isDynamicPrimary);
  const paymentStatus = isDynamicPrimary ? state.paymentStatus : po[7];
  const canInitiatePo = isDynamicPrimary && state.contractStatus === "合同完成" && poStatus === "未生成";
  const canPay = approvalStatus === "审批通过" && paymentStatus !== "付款完成";
  const poBackPage = state.role === "admin" ? "admin-pos" : state.role === "business" ? "business-pos" : "pos";
  return `
    ${pageHead("PO详情", "查看合同归档后由业务人员发起的 PO，以及付款状态和付款回传。", `
      <button class="btn" onclick="setPage('${poBackPage}')">返回PO列表</button>
      <button class="btn" ${canInitiatePo || state.role === "business" ? "" : "disabled"} onclick="state.poApprovalStatus='审批通过'; state.poStatus='已同步'; state.paymentStatus='待付款'; render()">模拟PO审批通过并同步台账</button>
      <button class="btn primary" ${canPay && isDynamicPrimary ? "" : "disabled"} onclick="state.paymentStatus='付款完成'; setPage('po-detail')">模拟付款完成回传</button>
    `)}
    ${summary(`${po[0]} · ${findPr(po[1])?.[2] || "采购订单"}`, `${tag(approvalStatus)} ${tag(paymentStatus)}`, `关联 ${po[1]} · ${po[2]} · ${po[3]}`)}
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>PO主信息</h3></div>
        ${infoGrid([
          ["PO编号", po[0]],
          ["关联PR", `<button class="btn link" onclick="selectPr('${po[1]}','${state.role === "business" ? "business-pr-detail" : "pr-detail"}')">${po[1]}</button>`],
          ["合同编号", po[2]],
          ["RFQ编号", po[3]],
          ["供应商", po[4]],
          ["PO类型", "费用类PO"],
          ["归集方式", "成本中心"],
          ["成本中心", "MKT-2026-BRAND"],
          ["销售订单", "-"],
          ["PO金额", po[5]],
          ["货币", poCurrency(po[5])],
          ["付款条款", "验收完成后 30 天付款"],
          ["发票类型", "增值税专用发票"],
          ["PO状态", tag(approvalStatus)],
          ["付款状态", tag(paymentStatus)],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>付款回传</h3></div>
        ${table(["付款节点", "状态", "金额", "说明"], [
          ["PO审批", tag(approvalStatus), po[5], approvalStatus === "审批通过" ? "Worklife BPM 审批已通过" : "等待 Worklife BPM 审批"],
          ["付款执行", tag(paymentStatus), paymentStatus === "付款完成" ? po[5] : paymentStatus === "部分付款" ? "¥60,000" : "¥0", paymentStatus === "付款完成" ? `付款流水 PAY-${po[0].replace("PO-", "")} 已回写` : "等待付款结果回传"],
          ["PR回写", tag(paymentStatus === "付款完成" ? "已回写" : "待回写"), paymentStatus === "付款完成" ? po[5] : "¥0", "付款状态和已付款金额展示在 PR 列表"],
        ])}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>PO明细</h3></div>
      ${table(["供应商", "物料/服务", "数量", "成本中心/销售订单", "最终单价", "小计", "税率", "含税小计"], [
        ["山西德利仁", "定制帆布袋", "3000", "MKT-2026-BRAND", "¥17.50", "¥52,500", "6%", "¥55,650"],
        ["上海博夷", "礼盒包装服务", "3000", "MKT-2026-BRAND", "¥4.50", "¥13,500", "6%", "¥14,310"],
      ])}
    </div>
  `;
}

function contractCreatePage() {
  const contractListPage = state.role === "admin" ? "admin-contracts" : "contracts";
  return `
    ${pageHead("创建合同", "优先选择 PR 或 PR 合集；如该 PR 关联多个已审批完成 RFQ，再选择对应 RFQ。")}
    ${summary("合同申请创建", `${tag("待选择PR")} ${tag("待填写合同")}`, "入口：合同管理列表顶部「创建合同」")}
    <div class="card">
      <div class="card-title"><h3>选择 PR / PR 合集和已完成 RFQ</h3><span class="hint">合同创建以 PR 为优先入口</span></div>
      ${table(["选择", "PR/合集", "RFQ编号", "项目名称", "采购策略", "报价方式", "核价总额", "审批状态", "供应商"], [
        [`<input type="radio" name="approvedRfq" checked />`, "PR-202605-001", "RFQ-202605-001", "2026 年品牌活动礼品采购", "询比价", tag("供应商在线报价"), "¥70,914", tag("已完成"), "山西德利仁、上海博夷"],
        [`<input type="radio" name="approvedRfq" />`, "PR-202605-008", "RFQ-202605-008", "仓储标签打印设备", "定向采购", tag("采购员代录报价"), "¥72,000", tag("已完成"), "南京云杉办公用品有限公司"],
        [`<input type="radio" name="approvedRfq" />`, "PR-202605-012", "RFQ-202605-012", "云监控订阅服务", "续约", tag("采购员代录报价"), "¥64,000", tag("已完成"), "上海博夷信息技术有限公司"],
      ])}
        <div class="notice" style="margin-top:14px;">选择 PR 后，系统自动带出已完成 RFQ、核价结果、供应商、物料明细、金额和货币。合同金额超过 PR 金额时仅红色风险提醒，不阻止提交。</div>
    </div>
    <div style="height:16px"></div>
    <div class="split">
      <div class="card">
        <div class="card-title"><h3>合同申请信息</h3></div>
        <div class="form-grid">
          ${field("合同名称", "品牌活动礼品采购合同")}
          ${field("合同申请编号", "CA-202605-001")}
          ${field("采购方", "CDP集团")}
          ${selectField("合同类型 Agreement Type", ["框架合同", "非框架合同"])}
          ${field("金额", "70914")}
          ${selectField("货币", ["CNY", "USD", "EUR"])}
          ${field("PR预算上限", "150000", true)}
          ${field("合同开始日期", "2026-05-20")}
          ${field("合同结束日期", "2026-06-30")}
          ${selectField("签署方式", ["电子签", "线下签署"])}
          ${selectField("审批路径", ["Worklife BPM审批", "Worklife BPM（目标方案，待IT评估）"])}
          ${field("TNC附件", "TNC_采购通用条款_v2026.pdf")}
          ${textareaField("付款条款", "按物料验收完成后 30 天内付款，供应商需开具增值税专用发票。")}
          ${textareaField("合同申请说明", "由 RFQ-202605-001 核价审批结果生成，带出最终中标供应商和最终中标明细。提交后发起 Worklife BPM 合同审批。")}
        </div>
        <div class="notice">金额风险提示：合同总金额超过 PR 金额或 PR 合集预算合计时展示红色风险提醒，但不阻止提交，也不强制填写风险备注。</div>
      </div>
      <div class="card">
        <div class="card-title"><h3>核价结果带出</h3></div>
        ${table(["选择", "中标供应商", "中标物料", "数量", "货币", "最终价", "小计"], winningSuppliers.map((r) => [`<input type="checkbox" checked />`, r[0], r[1], r[2], "CNY", r[3], r[4]]))}
        <div style="height:14px"></div>
        <div class="notice">MVP 中 TNC 作为合同附件上传，不做模块化条款库。若同一合同涉及多币种，建议按币种拆分为多份合同，以避免固定电子签模板无法动态展示多币种行项目。</div>
      </div>
    </div>
    <div class="sticky-actions">
      <button class="btn" onclick="setPage('${contractListPage}')">取消</button>
      <button class="btn" onclick="setContractStatus('合同草稿')">保存合同草稿</button>
      <button class="btn primary" onclick="setContractStatus('外部审批推送中')">生成合同申请并推送审批</button>
    </div>
  `;
}

function contractDetailPage() {
  let contract = findContract();
  const isDynamicPrimary = !contract && state.selectedPr === "PR-202605-001";
  if (isDynamicPrimary) {
    contract = ["CON-202605-013", "品牌活动礼品采购合同", "PR-202605-001", "RFQ-202605-001", "山西德利仁、上海博夷", "¥70,914", state.contractStatus === "未创建" ? "合同草稿" : state.contractStatus, prContractStatus("PR-202605-001")];
  }
  contract = contract || contracts[1];
  const status = isDynamicPrimary
    ? (state.contractStatus === "未创建" ? "合同草稿" : state.contractStatus)
    : (state.contractStatusByNo[contract[0]] || contract[6]);
  const actions = state.role === "admin"
    ? `<button class="btn" onclick="setPage('admin-contracts')">返回合同管理</button><button class="btn">下载归档件</button>`
    : state.role === "business"
      ? `<button class="btn" onclick="setPage('business-contracts')">返回我的合同</button><button class="btn primary" ${status === "合同完成" ? "" : "disabled"} onclick="state.selectedPr='${contract[2]}'; state.selectedContract='${contract[0]}'; setPage('business-po-create')">创建PO</button>`
      : contractActions(status);
  const desc = state.role === "admin" ? "采购经理查看合同申请、Worklife BPM 审批、电子签署和 PR 状态同步。" : state.role === "business" ? "业务人员查看合同签署和后续 PO 创建条件。" : "跟踪合同申请、Worklife BPM 审批、电子签署和 PR 状态同步。";
  const pr = findPr(contract[2]);
  return `
    ${pageHead("合同详情", desc, actions)}
    ${summary(`${contract[0]} · ${contract[1]}`, `${tag(status)} ${helpTip(contractStatusHelp(status))} ${tag(contract[2], "outline")}`, `关联 RFQ：${contract[3]} · 金额：${contractAmount(contract[5])} · 货币：${contractCurrency(contract[5])}`)}
    <div class="notice" style="margin-bottom:14px;">合同金额超过 PR 预算时展示红色风险提醒，但不阻断提交。签署方式支持电子签和线下签署归档；合同审批路径当前统一对接 Worklife BPM；历史审批接口仅作为数据兼容和迁移说明。</div>
    <div class="card">
      <div class="card-title"><h3>合同流程</h3><span>${tag(status)} ${helpTip(contractStatusHelp(status))}</span></div>
      ${contractFlow(status)}
    </div>
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>合同申请信息</h3></div>
        ${infoGrid([
          ["合同编号", contract[0]],
          ["合同名称", contract[1]],
          ["来源RFQ", contract[3]],
          ["PR预算", pr[4]],
          ["采购方", "CDP集团"],
          ["合同类型", "框架合同 / 非框架合同"],
          ["金额", contractAmount(contract[5])],
          ["货币", contractCurrency(contract[5])],
          ["当前合同状态", `${tag(status)} ${helpTip(contractStatusHelp(status))}`],
          ["签署方式", "电子签 / 线下签署可选"],
          ["TNC附件", "TNC_采购通用条款_v2026.pdf"],
          ["合同周期", "2026-05-20 至 2026-06-30"],
          ["PR同步状态", tag(prContractStatus(contract[2], contract[7]))],
          ["付款状态", tag(prPaymentStatus(contract[2]))],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>签署与归档</h3></div>
        ${table(["事项", "当前结果", "操作说明"], [
          ["Worklife BPM审批单", status === "合同草稿" ? "-" : "BPM-CON-202605-001", status === "外部审批推送中" ? "等待 Worklife BPM 接收" : "审批结果由 Worklife BPM 回传"],
          ["签署方式", "电子签 / 线下签署", "电子签由系统调用；线下签署由采购员上传归档并录入线下合同编号"],
          ["合同编号", status === "合同完成" ? "ESIGN-202605-001" : "待生成", "电子签合同完成后回传；线下合同由采购员录入"],
          ["后续动作", status === "合同完成" ? tag("可发起PO") : tag("待签署"), status === "合同完成" ? "可进入履约与付款发起 PO" : "等待合同完成归档"],
        ])}
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>中标供应商与合同明细</h3></div>
      ${table(["供应商", "物料/服务", "数量", "货币", "最终单价", "小计", "合同签署方"], winningSuppliers.map((r) => [r[0], r[1], r[2], "CNY", r[3], r[4], r[0]]))}
      <div class="notice" style="margin-top:14px;">合同签署归档后，系统将合同状态回写到采购申请单列表；随后由业务人员基于合同发起 PO，PO 可按成本中心或销售订单拆分，并经 Worklife BPM 审批后同步台账。</div>
    </div>
  `;
}

function contractActions(status) {
  if (status === "合同草稿") {
    return `<button class="btn" onclick="setPage('contract-create')">编辑申请</button><button class="btn primary" onclick="setContractStatus('外部审批推送中')">推送Worklife BPM审批</button>`;
  }
  if (status === "外部审批推送中") {
    return `<button class="btn" onclick="openModal('oaPending')">查看推送任务</button><button class="btn primary" onclick="setContractStatus('Worklife BPM审批中')">模拟Worklife BPM接收成功</button>`;
  }
  if (status === "Worklife BPM审批中") {
    return `<button class="btn" onclick="openModal('oaPending')">查看Worklife BPM审批</button><button class="btn primary" onclick="setContractStatus('Worklife BPM审批通过')">模拟Worklife BPM审批通过</button>`;
  }
  if (status === "Worklife BPM审批通过") {
    return `<button class="btn primary" onclick="setContractStatus('待签署')">选择电子签</button><button class="btn" onclick="openModal('offlineArchive')">上传线下签署文档并归档</button>`;
  }
  if (status === "待签署") {
    return `<button class="btn primary" onclick="setContractStatus('电子签署中')">发起电子签署</button><button class="btn" onclick="openModal('offlineArchive')">改为线下签署并上传归档</button>`;
  }
  if (status === "电子签署中") {
    return `<button class="btn" onclick="openModal('esignPending')">查看签署进度</button><button class="btn primary" onclick="setContractStatus('合同完成')">模拟签署完成并归档</button>`;
  }
  return `<button class="btn" onclick="setPage('purchase-requests')">查看PR同步结果</button><button class="btn primary" onclick="setPage('po-detail')">查看PO</button><button class="btn">下载合同归档件</button>`;
}

function contractFlow(status) {
  const steps = [
    ["合同草稿", "选择已审批完成 RFQ 并填写合同申请"],
    ["外部审批推送中", "将合同申请推送至 Worklife BPM"],
    ["Worklife BPM审批中", "Worklife BPM 合同审批中，等待审批结果回传"],
    ["Worklife BPM审批通过", "Worklife BPM审批通过，进入待签署"],
    ["待签署", "选择电子签或上传线下签署归档"],
    ["电子签署中", "采购方与供应商电子签署"],
    ["合同完成", "合同归档并同步 PR 状态"],
  ];
  const order = ["合同草稿", "外部审批推送中", "Worklife BPM审批中", "Worklife BPM审批通过", "待签署", "电子签署中", "合同完成"];
  const current = order.indexOf(status);
  return `<div class="flow-steps">${steps.map(([name, desc], index) => {
    const stepIndex = order.indexOf(name);
    const cls = current > stepIndex ? "done" : current === stepIndex ? "active" : "";
    return `<div class="flow-step ${cls}"><strong>${index + 1}. ${name}</strong><span>${desc}</span></div>`;
  }).join("")}</div>`;
}

function supplierDashboard() {
  return `
    ${pageHead("供应商工作台", "维护企业资料，处理询价与报价待办。")}
    ${metrics([["企业信息", "100%"], ["联系人", "100%"], ["证件", "80%"], ["待报价", "1"], ["已完成询价", "5"]])}
    <div style="height:16px"></div>
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>待处理事项</h3></div>
        ${table(["事项", "状态", "操作"], [
          ["完税证明待审核", tag("待审核信息"), `<button class="btn link" onclick="setPage('supplier-certs')">查看</button>`],
          ["营业执照 30 天后到期", tag("即将到期"), `<button class="btn link" onclick="setPage('supplier-certs')">更新</button>`],
          ["2026 年品牌活动礼品采购待报价", tag("待报价"), `<button class="btn link" onclick="setPage('supplier-quote')">去报价</button>`],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>快捷入口</h3></div>
        <div class="actions">
          <button class="btn" onclick="setPage('supplier-company')">编辑企业信息</button>
          <button class="btn" onclick="setPage('supplier-certs')">管理证件</button>
          <button class="btn" onclick="setPage('supplier-contacts')">管理联系人</button>
          <button class="btn primary" onclick="setPage('supplier-rfqs')">查看询价单</button>
        </div>
      </div>
    </div>
  `;
}

function supplierCompanyPage() {
  return `
    ${pageHead("企业信息", "供应商在此独立维护企业信息和证件资料，提交后由采购员审核。")}
    <div class="notice">当前状态：${tag("待完善信息", "orange")} · 企业信息、银行信息和证件资料在同一流程中维护；提交后由采购员审核。</div>
    <div style="height:16px"></div>
    <div class="card">
      ${steps(["企业基础信息", "银行信息", "证件资料", "提交审核"], 3)}
      <div class="form-grid">
        ${field("供应商ID号", "VD 0001", true)}
        ${field("供应商名称", "上海博夷信息技术有限公司")}
        ${field("公司法人", "沈家伟")}
        ${field("注册时间", "2016-01-07")}
        ${field("注册资金", "3000000")}
        ${field("一般纳税人", "是")}
        ${textareaField("公司地址", "上海市奉贤区奉浦大道1599号第一幢第一层1-17室")}
        ${textareaField("主力客户", "威高集团、宝尊电商、唯德康、万鲤商联、长安金融")}
        ${field("户名", "上海博夷信息技术有限公司")}
        ${field("开户银行", "招商银行股份有限公司上海虹桥商务区支行")}
        ${field("银行账号", "121919443310602")}
      </div>
      <div style="height:16px"></div>
      <div class="card-title"><h3>证件资料</h3><button class="btn" onclick="setPage('supplier-cert-upload')">上传/更新证件</button></div>
      ${table(["证件类型", "文件名", "有效期截止", "审核状态", "到期状态", "操作"], [
        ["营业执照", "营业执照_上海博夷.pdf", "2036-01-06", tag("已通过"), tag("正常"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">更新</button>`],
        ["完税证明", "完税证明_2025.pdf", "2026-05-20", tag("待审核信息"), tag("即将到期"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">查看</button>`],
        ["食品经营许可证", "-", "-", tag("待上传"), tag("待上传"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">上传</button>`],
      ])}
      <div class="sticky-actions"><button class="btn">保存草稿</button><button class="btn primary">提交审核</button></div>
    </div>
  `;
}

function supplierSimpleList(type) {
  const map = {
    "supplier-certs": ["证件管理", ["证件类型", "文件名", "有效期截止", "审核状态", "到期状态", "操作"], [
      ["营业执照", "营业执照_上海博夷.pdf", "2036-01-06", tag("已通过"), tag("正常"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">更新</button>`],
      ["完税证明", "完税证明_2025.pdf", "2026-05-20", tag("待审核信息"), tag("即将到期"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">查看</button>`],
      ["食品经营许可证", "食品经营许可证.pdf", "2025-12-31", tag("已驳回"), tag("正常"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">重新上传</button>`],
      ["医疗机构执业许可证", "-", "-", tag("待上传"), tag("待上传"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">上传</button>`],
      ["完税证明", "完税证明_2024.pdf", "2026-05-05", tag("已通过"), tag("已过期"), `<button class="btn link" onclick="setPage('supplier-cert-upload')">更新</button>`],
    ]],
    "supplier-contacts": ["联系人管理", ["姓名", "职务", "手机号", "邮箱", "主要联系人", "操作"], [
      ["沈家伟", "总经理", "13321832330", "jerry.shen@iboye.com", tag("是", "green"), `<button class="btn link" onclick="setPage('supplier-contact-edit')">编辑</button>`],
      ["邹钰", "会计主管", "18651960886", "zoe.zou@insee-tech.com", tag("否", "gray"), `<button class="btn link" onclick="setPage('supplier-contact-edit')">编辑</button>`],
    ]],
    "supplier-rfqs": ["我的询价单", ["RFQ编号", "项目名称", "采购公司", "报价截止", "状态", "我的报价", "操作"], supplierRfqRows.map((r) => [
      r[0], r[1], r[2], r[3], tag(r[4]), tag(r[5]), r[5] === "待报价" ? `<button class="btn link" onclick="setPage('supplier-rfq-detail')">详情</button><button class="btn link" onclick="setPage('supplier-quote')">去报价</button>` : `<button class="btn link" onclick="setPage('supplier-rfq-detail')">查看</button>`,
    ])],
  };
  const [title, heads, rows] = map[type];
  const action = type === "supplier-certs"
    ? `<button class="btn primary" onclick="setPage('supplier-cert-upload')">上传证件</button>`
    : type === "supplier-contacts"
      ? `<button class="btn primary" onclick="setPage('supplier-contact-edit')">新增联系人</button>`
      : "";
  const filterHtml = type === "supplier-certs"
    ? filters(["证件类型", { label: "审核状态", options: stateOptions.reviewStatus.concat(["待上传"]) }, { label: "到期状态", options: stateOptions.certExpiry }])
    : type === "supplier-rfqs"
      ? filters(["RFQ编号 / 项目名称", { label: "RFQ状态", options: stateOptions.rfq }, { label: "我的报价", options: stateOptions.quote }])
      : filters(["联系人姓名", { label: "是否主要联系人", options: ["是", "否"] }]);
  const f = currentFilters();
  const filteredRows = rows.filter((row) =>
    textIncludes(row.map(stripHtml), f.f0) &&
    (!f.f1 || row.some((cell) => stripHtml(cell).includes(f.f1))) &&
    (!f.f2 || row.some((cell) => stripHtml(cell).includes(f.f2)))
  );
  const gateNotice = type === "supplier-rfqs" ? `<div class="notice" style="margin-bottom:14px;">企业信息维护与询价报价流程相互独立：本页只处理询价查看和报价操作，企业信息请从左侧「企业信息」菜单进入维护。</div>` : "";
  return pageHead(title, "供应商仅可查看本企业相关数据。", action) + gateNotice + filterHtml + tableWrap(heads, filteredRows);
}

function supplierQuotePage() {
  return `
    ${pageHead("报价 - 2026 年品牌活动礼品采购", "按照固定供应商报价单模板填写税务信息、付款条件和报价明细。")}
    ${summary("RFQ-202605-001", `${tag("报价中")} ${tag("距离截止 3天04小时", "orange")}`, "可报价时间：2026-05-06 09:00 ~ 2026-05-12 18:00 · 项目预算：不展示")}
    <div class="notice" style="margin-bottom:14px;">企业信息维护与询价报价流程相互独立；本页只处理报价字段、附件和提交记录。</div>
    <div class="notice" style="margin-bottom:14px;">报价规则：供应商在报价截止前仅可提交一次报价，提交后不可自行修改；仅当采购员主动驳回报价后，供应商才可重新提交。线上字段与线下「供应商报价单模版.xlsx」保持一致。</div>
    <div class="grid cols-2" style="margin-bottom:14px;">
      <div class="notice"><strong>采购信息</strong><br/>项目名称：2026 年品牌活动礼品采购<br/>PR号：PR-202605-001<br/>采购公司：CDP集团<br/>采购员：张伟</div>
      <div class="notice"><strong>供应商信息</strong><br/>供应商名称：上海博夷信息技术有限公司<br/>联系人：沈家伟<br/>电话：13321832330<br/>邮箱：jerry.shen@iboye.com</div>
    </div>
    <div class="card">
      <div class="form-grid">
        ${field("税率", "6%")}
        ${field("询价币别", "CNY/人民币")}
        ${field("可接受账期（天）", "30")}
        ${selectField("付款方式", ["后结算", "预付", "充值"])}
        ${selectField("发票类型", ["增值税专用发票", "普通发票", "形式发票"])}
      </div>
      <div style="height:16px"></div>
      <div class="card-title"><h3>报价明细</h3><button class="btn" onclick="openModal('quoteLine')">添加报价明细行</button></div>
      <div class="notice" style="margin-bottom:14px;">下方默认带出采购员设置的报价内容。供应商可直接修改单价和交货天数，也可以点击「添加报价明细行」补充运费、安装费、折扣或其他服务项。</div>
      ${table(["序号", "物料/服务名称", "数量", "单价", "小计", "服务/交货天数", "操作"], [
        ["1", `<input class="input" value="定制帆布袋" />`, `<input class="input" value="3000" />`, `<input class="input" value="17.50" />`, `<input class="input" value="52,500.00" readonly />`, `<input class="input" value="18天" />`, `<button class="btn link" onclick="openModal('quoteLine')">编辑</button><button class="btn link danger" onclick="showToast('已删除该报价明细行')">删除</button>`],
        ["2", `<input class="input" value="礼盒包装服务" />`, `<input class="input" value="3000" />`, `<input class="input" value="5.00" />`, `<input class="input" value="15,000.00" readonly />`, `<input class="input" value="15天" />`, `<button class="btn link" onclick="openModal('quoteLine')">编辑</button><button class="btn link danger" onclick="showToast('已删除该报价明细行')">删除</button>`],
        ["3", `<input class="input" value="上海市内配送费" />`, `<input class="input" value="1" />`, `<input class="input" value="0.00" />`, `<input class="input" value="0.00" readonly />`, `<input class="input" value="随货交付" />`, `<button class="btn link" onclick="openModal('quoteLine')">编辑</button><button class="btn link danger" onclick="showToast('已删除该报价明细行')">删除</button>`],
      ])}
      <div class="notice" style="margin-top:14px;">合计未税：${money("¥67,500")} · 税率：6% · 合计含税：${money("¥71,550")}</div>
      <div class="field" style="margin-top:14px;"><label>备注</label><textarea class="textarea">报价有效期 30 天，含配送至上海办公室。</textarea></div>
      <div class="field"><label>附件</label><input class="input" value="报价单_上海博夷.pdf" /></div>
      <label class="check-row"><input type="checkbox" checked /> 供应商对其所提供的上述报价保证真实、准确且有效，并承诺承担因报价不实或错误所引发的责任后果。</label>
      <div class="sticky-actions"><button class="btn" onclick="setPage('supplier-rfq-detail')">取消</button><button class="btn primary" onclick="openModal('quoteSubmitted')">确认提交报价</button></div>
    </div>
  `;
}

function supplierCreatePage() {
  return `
    ${pageHead("创建供应商", "采购员创建供应商账号，可仅保存为创建成功，也可保存并发送入驻邀请邮件。")}
    <div class="notice" style="margin-bottom:14px;">基础账号字段来自「表单设置 / 供应商创建表单字段库」，后续字段调整只影响供应商创建表单。</div>
    <div class="split">
      <div class="card">
        <div class="card-title"><h3>基础账号信息</h3></div>
        <div class="form-grid">
          ${field("供应商企业名称", "上海示例供应商有限公司")}
          ${field("联系人姓名", "王强")}
          ${field("联系人手机号", "13900000000")}
          ${field("联系人邮箱", "wangqiang@example.com")}
          ${field("联系人备注", "可填写对接范围，例如报价、合同、付款沟通")}
          ${field("关联采购员", "张伟", true)}
        </div>
        <div class="sticky-actions">
          <button class="btn" onclick="setPage('suppliers')">取消</button>
          <button class="btn" onclick="openModal('supplierSaved')">仅保存（创建成功）</button>
          <button class="btn primary" onclick="openModal('inviteSent')">保存并发送邀请</button>
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>创建后系统动作</h3></div>
        <div class="timeline">
          ${timeline("生成供应商 ID", "规则：VD + 4 位自增序号，例如 VD 0005")}
          ${timeline("创建供应商登录账号", "手机号作为登录凭据，初始密码为随机 10 位")}
          ${timeline("写入创建信息", "记录创建人、创建时间和当前供应商状态")}
          ${timeline("状态流转", "仅保存为创建成功；发送邀请后变为待进入")}
        </div>
      </div>
    </div>
  `;
}

function supplierEditPage() {
  return `
    ${pageHead("编辑供应商", "采购员编辑供应商信息会直接生效，并写入变更记录。")}
    <div class="notice" style="margin-bottom:14px;">当前表单由「表单设置 / 供应商企业信息表单」渲染。字段变更会按字段编码生成变更记录，便于审核和追溯。</div>
    <div class="card">
      <div class="form-grid">
        ${field("供应商ID号", "VD 0001", true)}
        ${field("供应商名称", "上海博夷信息技术有限公司")}
        ${field("公司法人", "沈家伟")}
        ${field("注册时间", "2016-01-07")}
        ${field("注册资金", "3000000")}
        ${selectField("一般纳税人", ["是", "否"])}
        ${textareaField("公司地址", "上海市奉贤区奉浦大道1599号第一幢第一层1-17室")}
        ${textareaField("主力客户", "威高集团、宝尊电商、唯德康、万鲤商联、长安金融")}
        ${field("户名", "上海博夷信息技术有限公司")}
        ${field("开户银行名称", "招商银行股份有限公司上海虹桥商务区支行")}
        ${field("银行账号", "121919443310602")}
      </div>
      <div class="notice" style="margin-top:14px;">保存后无需供应商确认，系统自动生成逐字段变更记录。</div>
      <div class="sticky-actions"><button class="btn" onclick="setPage('supplier-detail')">取消</button><button class="btn primary" onclick="setPage('supplier-detail')">保存修改</button></div>
    </div>
  `;
}

function buyerCertUploadPage() {
  return `
    ${pageHead("手动添加供应商证件", "采购员代供应商维护证件主数据，保存后直接生效为已通过。")}
    ${summary("上海博夷信息技术有限公司", `${tag("合作中")} ${tag("采购员维护", "blue")}`, "供应商ID：VD 0001 · 当前证件状态：正常")}
    <div class="notice" style="margin-bottom:14px;">证件字段来自「表单设置 / 供应商证件上传表单」。不同证件类型可显示不同扩展字段。</div>
    <div class="split">
      <div class="card">
        <div class="card-title"><h3>证件信息</h3></div>
        <div class="form-grid">
          ${selectField("证件类型", ["营业执照", "完税证明", "医疗机构执业许可证", "食品经营许可证"])}
          ${field("证件文件", "完税证明_2026.pdf")}
          ${field("有效期开始", "2026-01-01")}
          ${field("有效期截止", "2026-12-31")}
          ${selectField("处理方式", ["更新当前有效证件", "新增一份历史证件"])}
          ${field("来源", "采购员手动添加", true)}
          ${textareaField("备注", "供应商已通过邮件提供证件扫描件，由采购员代为上传维护。")}
        </div>
        <div class="notice" style="margin-top:14px;">保存后审核状态为已通过，不进入审核中心；系统记录维护人张伟和维护时间，并纳入证件到期提醒。</div>
        <div class="sticky-actions">
          <button class="btn" onclick="setPage('supplier-detail')">取消</button>
          <button class="btn">保存并继续添加</button>
          <button class="btn primary" onclick="state.supplierTab='certs'; setPage('supplier-detail')">保存证件</button>
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>文件预览</h3></div>
        <div class="file-preview">
          <div class="file-icon">PDF</div>
          <strong>完税证明_2026.pdf</strong>
          <span>支持 PDF、JPG、PNG，单个文件不超过 10MB。</span>
        </div>
      </div>
    </div>
  `;
}

function reviewInfoDetailPage() {
  const backPage = state.role === "admin" ? "admin-approval" : "reviews";
  return `
    ${pageHead("信息变更审核详情", "采购员审核供应商提交的企业信息变更。")}
    ${summary("山西德利仁信息技术服务有限公司", `${tag("待审核信息")}`, "提交人：高阳阳 · 提交时间：2026-05-05 09:30")}
    <div class="split">
      <div class="card">
        <div class="card-title"><h3>字段变更对比</h3></div>
        ${table(["字段", "变更前", "变更后", "差异"], [
          ["开户银行", "中国建设银行股份有限公司朔州支行", "中国建设银行股份有限公司朔州鄯阳街支行", tag("已修改", "orange")],
          ["银行账号", "14050166510800000001", "14050166510800000781", tag("已修改", "orange")],
          ["主力客户", "中国人寿、恒生银行", "中国人寿、恒生银行、中国民生银行", tag("已新增", "blue")],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>审核操作</h3></div>
        <div class="field"><label>审核意见</label><textarea class="textarea" placeholder="通过可不填，驳回时建议说明原因"></textarea></div>
        <div class="notice">通过后变更内容将正式写入供应商主数据；驳回后供应商可修改后重新提交。</div>
        <div class="sticky-actions"><button class="btn" onclick="setPage('${backPage}')">返回</button><button class="btn danger" onclick="openModal('reviewRejected')">驳回</button><button class="btn primary" onclick="openModal('reviewApproved')">通过</button></div>
      </div>
    </div>
  `;
}

function reviewCertDetailPage() {
  const backPage = state.role === "admin" ? "admin-approval" : "reviews";
  return `
    ${pageHead("证件审核详情", "查看证件文件、有效期和供应商信息后进行审核。")}
    ${summary("上海博夷信息技术有限公司 · 完税证明", `${tag("待审核信息")} ${tag("即将到期")}`, "上传人：沈家伟 · 上传时间：2026-05-05 10:20")}
    <div class="split">
      <div class="card">
        <div class="card-title"><h3>文件预览</h3><button class="btn">下载原件</button></div>
        <div class="file-preview">
          <div class="file-icon">PDF</div>
          <strong>完税证明_2025.pdf</strong>
          <span>这里是证件文件预览占位。真实系统中可嵌入 PDF/JPG 预览。</span>
        </div>
      </div>
      <div class="card">
        <div class="card-title"><h3>证件信息</h3></div>
        ${infoGrid([
          ["证件类型", "完税证明"],
          ["有效期开始", "2025-01-01"],
          ["有效期截止", "2026-05-20"],
          ["文件大小", "2.4 MB"],
          ["文件格式", "PDF"],
          ["供应商状态", "合作中"],
        ])}
        <div class="field" style="margin-top:14px;"><label>审核意见</label><textarea class="textarea" placeholder="驳回时填写原因"></textarea></div>
        <div class="sticky-actions"><button class="btn" onclick="setPage('${backPage}')">返回</button><button class="btn danger" onclick="openModal('reviewRejected')">驳回</button><button class="btn primary" onclick="openModal('reviewApproved')">通过</button></div>
      </div>
    </div>
  `;
}

function prDetailTabButton(key, label) {
  return `<button class="tab-btn ${state.prTab === key ? "active" : ""}" onclick="state.prTab='${key}'; render()">${label}</button>`;
}

function prDetailPage() {
  const pr = findPr();
  const contractListPage = state.role === "admin" ? "admin-contracts" : "contracts";
  const rfq = rfqs.find((r) => r[2] === pr[0]);
  const contract = contracts.find((c) => c[2] === pr[0]);
  const relatedPos = relatedPoRowsForPr(pr[0]);
  const paidTotal = paidTotalForPr(pr[0]);
  const bundleRows = prBundleItems[pr[0]] || [];
  const bundleLocked = hasGeneratedRfq(pr[0]);
  const action = `<button class="btn" onclick="setPage('purchase-requests')">返回列表</button><button class="btn primary" onclick="selectPr('${pr[0]}','create-rfq')">创建询价管理单</button>`;
  const overviewPanel = `
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>PR 基本信息</h3></div>
        ${infoGrid([
          ["PR类型", tag(prType(pr), isPrBundle(pr) ? "purple" : "gray")],
          ["申请部门", pr[1]],
          ["申请人", pr[5]],
          ["数量", pr[3]],
          ["预算", pr[4]],
          ["希望完成时间", pr[6]],
          ["PR状态", tag(prProcessStatus(pr))],
          ["合同状态", tag(prContractStatus(pr[0], pr[8]))],
          ["PO状态", tag(poApprovalStatus(relatedPos[0]))],
          ["付款状态", tag(prPaymentStatus(pr[0]))],
          ["需求内容", `${pr[2]}，数量 ${pr[3]}，预算 ${pr[4]}`],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>需求内容</h3></div>
        <p class="paragraph">${pr[2]}，数量 ${pr[3]}，预算 ${pr[4]}。需求由业务人员在采购系统发起，Worklife BPM 审批通过并分配采购员后，作为合同、PO 和付款回传的业务来源；RFQ 为采购内部处理过程。</p>
      </div>
    </div>
  `;
  const bundlePanel = bundleRows.length ? `
    <div class="card">
      <div class="card-title"><h3>合集包含的采购申请单</h3><span class="hint">${bundleLocked ? "已生成RFQ，合集明细已冻结" : "未生成RFQ，可移除明细"}</span></div>
      ${table(bundleLocked ? ["原始PR号", "部门", "内容摘要", "预算", "申请人", "希望完成时间", "合并状态"] : ["原始PR号", "部门", "内容摘要", "预算", "申请人", "希望完成时间", "合并状态", "操作"], bundleRows.map((row) => bundleLocked ? [
        row[0], row[1], row[2], row[3], row[4], row[5], tag(row[6], "purple"),
      ] : [
        row[0], row[1], row[2], row[3], row[4], row[5], tag(row[6], "purple"), `<button class="btn link danger-link" onclick="openModal('removeBundlePr')">移除</button>`,
      ]))}
      <div class="notice" style="margin-top:14px;">${bundleLocked ? "该 PR 合集已经生成采购询价单，因此不允许移除合集内的原始 PR。" : "该 PR 合集尚未生成采购询价单，可在详情中移除某条原始 PR；移除后合集预算将重新计算，原始 PR 可重新回到可选来源。"}</div>
    </div>
  ` : `
    <div class="card">
      <div class="card-title"><h3>包含PR</h3></div>
      <div class="empty">当前为普通 PR，不包含下级采购申请单。</div>
    </div>
  `;
  const rfqPanel = rfq ? `
    <div class="card">
      <div class="card-title"><h3>RFQ 信息</h3><button class="btn primary" onclick="selectRfq('${rfq[0]}')">查看RFQ</button></div>
      ${infoGrid([
        ["RFQ编号", rfq[0]],
        ["项目名称", rfq[1]],
        ["采购策略", rfq[3]],
        ["报价方式", tag(rfq[4])],
        ["RFQ状态", tag(pr[0] === "PR-202605-001" ? state.rfqStatus : rfq[5])],
        ["参与供应商", rfq[7]],
      ])}
    </div>
  ` : `
    <div class="card">
      <div class="card-title"><h3>RFQ 信息</h3><button class="btn primary" onclick="selectPr('${pr[0]}','create-rfq')">创建询价管理单</button></div>
      <div class="empty">当前 PR 尚未创建 RFQ。点击创建询价单后进入询价单创建流程。</div>
    </div>
  `;
  const contractPanel = contract ? `
    <div class="card">
      <div class="card-title"><h3>合同信息</h3>${contract[0] === "-" ? `<button class="btn primary" onclick="selectContract('${contract[0]}','${pr[0]}','contract-create')">创建合同</button>` : `<button class="btn primary" onclick="selectContract('${contract[0]}','${pr[0]}')">查看合同</button>`}</div>
      ${infoGrid([
        ["合同编号", contract[0]],
        ["合同名称", contract[1]],
        ["关联RFQ", contract[3]],
        ["供应商", contract[4]],
        ["金额", contractAmount(contract[5])],
        ["货币", contractCurrency(contract[5])],
        ["合同状态", tag(contract[6])],
      ])}
    </div>
  ` : `
    <div class="card">
      <div class="card-title"><h3>合同信息</h3><button class="btn" onclick="setPage('${contractListPage}')">进入合同管理</button></div>
      <div class="empty">当前 PR 尚未生成合同。核价审批通过后，可在合同管理中创建合同申请。</div>
    </div>
  `;
  const poPanel = relatedPos.length ? `
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>关联PO列表</h3><span class="hint">一个 PR 可关联多个 PO</span></div>
        ${table(["PO编号", "关联合同", "关联RFQ", "供应商", "PO金额", "PO状态", "付款状态", "已付款金额", "操作"], relatedPos.map((p) => [
          `<button class="btn link" onclick="selectPo('${p[0]}')">${p[0]}</button>`,
          p[2],
          p[3],
          p[4],
          p[5],
          tag(poApprovalStatus(p)),
          tag(p[7]),
          formatCny(paidAmountForPo(p)),
          `<button class="btn link" onclick="selectPo('${p[0]}')">查看PO</button>`,
        ]))}
      </div>
      <div class="card">
        <div class="card-title"><h3>付款汇总</h3></div>
        ${infoGrid([
          ["关联PO数量", String(relatedPos.length)],
          ["PO状态", tag(poApprovalStatus(relatedPos[0]))],
          ["付款状态", tag(prPaymentStatus(pr[0]))],
          ["已付款金额", paidTotal],
          ["查看规则", "点击左侧任一 PO 编号进入 PO 详情"],
        ])}
      </div>
    </div>
  ` : `
    <div class="card">
      <div class="card-title"><h3>PO 与付款</h3></div>
      <div class="empty">合同签署归档后，由业务基于 PR、合同和最终中标结果发起 PO；付款结果由台账系统回传。</div>
    </div>
  `;
  const panels = {
    overview: overviewPanel,
    bundle: bundlePanel,
    rfq: rfqPanel,
    contract: contractPanel,
    po: poPanel,
  };
  return `
    ${pageHead("采购申请单详情", "查看业务人员发起并审批通过的采购需求，并追踪 RFQ、合同、PO 和付款闭环。", action)}
    ${summary(`${pr[0]} · ${pr[2]}`, `${tag(prProcessStatus(pr))}`, "来源：业务人员发起 · Worklife BPM 审批通过并分配采购员")}
    <div class="tabs pr-tabs">
      ${prDetailTabButton("overview", "PR概览")}
      ${prDetailTabButton("bundle", "包含PR")}
      ${prDetailTabButton("rfq", "RFQ")}
      ${prDetailTabButton("contract", "合同")}
      ${prDetailTabButton("po", "PO与付款")}
    </div>
    ${panels[state.prTab] || overviewPanel}
  `;
}

function rfqEditPage() {
  return `
    ${pageHead("编辑询价单", "草稿可编辑全部内容；报价中若已有供应商报价，核心报价口径需开启新轮或退回报价后再调整。")}
    <div class="notice">当前状态：${tag(state.rfqStatus)}。报价中若尚无供应商提交报价，可编辑并通知所有通知人；已有报价时核心字段锁定，只允许开启新轮或退回受影响报价后处理。</div>
    <div style="height:16px"></div>
    <div class="card">
      ${createStepBase()}
      <div style="height:12px"></div>
      ${createStepSchedule()}
      <div class="sticky-actions"><button class="btn" onclick="setPage('rfq-detail')">取消</button><button class="btn">保存草稿</button><button class="btn primary" onclick="setPage('rfq-detail')">保存并按规则通知</button></div>
    </div>
  `;
}

function quoteDetailPage() {
  return `
    ${pageHead("报价详情", "采购员查看单个供应商报价。开标前真实系统应屏蔽金额，本原型用于开标后详情展示。")}
    ${summary("上海博夷信息技术有限公司 · RFQ-202605-001", `${tag("已报价")} ${tag("非代询价", "gray")}`, "提交时间：2026-05-08 14:20 · 通知人：沈家伟")}
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>报价条件</h3></div>
        ${infoGrid([
          ["税率", "6%"],
          ["币别", "CNY"],
          ["账期", "30 天"],
          ["付款方式", "后结算"],
          ["发票类型", "增值税专用发票"],
          ["附件", "报价单_上海博夷.pdf"],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>备注</h3></div>
        <p class="paragraph">报价有效期 30 天，含配送至上海办公室。打样周期 3 天，确认后开始批量生产。</p>
      </div>
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>报价明细</h3><button class="btn" onclick="openModal('returnQuote')">退回报价</button></div>
      ${table(["物料/服务", "数量", "单位", "单价未税", "小计未税", "交货/服务天数"], quoteRows)}
      <div class="notice" style="margin-top:14px;">合计未税：${money("¥67,500")} · 合计含税：${money("¥71,550")}</div>
    </div>
  `;
}

function proxyQuotePage() {
  return `
    ${pageHead("代询价", "采购员代替供应商填写报价，必须上传供应商报价附件作为证据。")}
    ${summary("山西德利仁信息技术服务有限公司", `${tag("待报价")} ${tag("代询价", "blue")}`, "RFQ-202605-001 · 当前状态：报价中")}
    <div class="card">
      <div class="notice">代询价提交后，与供应商自行提交报价一样进入“已报价”状态，并记录代询价操作人。</div>
      <div style="height:16px"></div>
      <div class="form-grid">
        ${field("税率", "6%")}
        ${field("币别", "CNY")}
        ${field("账期", "45 天")}
        ${selectField("付款方式", ["后结算", "预付", "充值"])}
        ${selectField("发票类型", ["增值税专用发票", "普通发票", "形式发票"])}
        ${field("供应商报价附件", "山西德利仁_报价邮件.pdf")}
      </div>
      <div style="height:16px"></div>
      ${table(["物料/服务", "数量", "单位", "单价未税", "小计未税", "交货/服务天数"], [
        ["定制帆布袋", "3000", "个", `<input class="input" value="17.50" />`, "¥52,500", `<input class="input" value="18天" />`],
        ["礼盒包装服务", "3000", "套", `<input class="input" value="4.80" />`, "¥14,400", `<input class="input" value="18天" />`],
      ])}
      <div class="sticky-actions"><button class="btn" onclick="setPage('rfq-detail')">取消</button><button class="btn primary" onclick="setPage('rfq-detail')">提交代询价</button></div>
    </div>
  `;
}

function newRoundPage() {
  return `
    ${pageHead("开启新一轮报价", "基于已开标结果开启下一轮议价，保留历史轮次记录。")}
    ${summary("RFQ-202605-001 · 第2轮", `${tag("新轮次")}`, "上一轮：第1轮已开标 · 参与供应商：3 家")}
    <div class="card">
      <div class="form-grid">
        ${field("新报价开始时间", "2026-05-14 09:00")}
        ${field("新报价结束时间", "2026-05-16 18:00")}
        ${field("新开标时间", "2026-05-17 10:00")}
        ${textareaField("本轮说明", "请各供应商基于第一轮报价结果提交最终报价。")}
      </div>
      <div style="height:16px"></div>
      ${createStepSuppliers()}
      <div class="sticky-actions"><button class="btn" onclick="setPage('bid-summary')">取消</button><button class="btn primary" onclick="state.rfqStatus='报价中'; setPage('rfq-detail')">开启并通知供应商</button></div>
    </div>
  `;
}

function supplierRfqDetailPage() {
  return `
    ${pageHead("询价单详情", "供应商仅能查看发给本企业的询价单和本方报价。", `<button class="btn" onclick="setPage('supplier-rfqs')">返回我的询价单</button><button class="btn primary" onclick="setPage('supplier-quote')">去报价</button>`)}
    ${summary("RFQ-202605-001 · 2026 年品牌活动礼品采购", `${tag("报价中")} ${tag("我的报价：已退回", "orange")}`, "采购公司：CDP集团 · 截止时间：2026-05-12 18:00")}
    <div class="notice" style="margin-bottom:14px;">上一次报价被采购员退回，退回原因：报价明细缺少交货周期说明，请补充后重新提交。企业信息维护请在「企业信息」菜单独立处理，本页不再提供完善企业信息入口。</div>
    <div class="card">
      ${infoGrid([
        ["PR号", "PR-202605-001"],
        ["采购公司", "CDP集团"],
        ["可报价时间", "2026-05-06 09:00 ~ 2026-05-12 18:00"],
        ["开标时间", "2026-05-13 10:00"],
        ["项目预算", "不展示"],
        ["采购需求", "采购定制帆布袋与礼盒包装服务，用于 2026 年品牌活动。"],
      ])}
    </div>
    <div style="height:16px"></div>
    <div class="card">
      <div class="card-title"><h3>我的报价记录</h3></div>
      ${table(["提交时间", "报价金额", "状态", "退回原因", "操作"], [
        ["2026-05-10 15:30", "¥71,550", tag("已退回", "orange"), "报价明细缺少交货周期说明，请补充后重新提交。", `<button class="btn link" onclick="setPage('supplier-quote')">重新报价</button>`],
      ])}
    </div>
  `;
}

function supplierContractsPage() {
  const rows = [
    ["CON-202605-013", "品牌活动礼品采购合同", "RFQ-202605-001", "CDP集团", "¥70,914", "待签署", "2026-05-15"],
    ["CON-202605-004", "上海办公室绿植租摆服务合同", "RFQ-202605-003", "CDP集团", "¥58,512", "已签署", "2026-05-02"],
  ];
  return `
    ${pageHead("合同管理", "展示当前供应商需要签署和已经签署的合同。")}
    ${filters(["合同编号 / 合同名称 / RFQ", { label: "合同状态", options: ["待签署", "电子签署中", "已签署"] }])}
    ${tableWrap(["合同编号", "合同名称", "关联RFQ", "采购公司", "金额", "货币", "状态", "更新时间", "操作"], rows.map((r) => [
      `<button class="btn link" onclick="setPage('supplier-contract-detail')">${r[0]}</button>`, r[1], r[2], r[3], contractAmount(r[4]), contractCurrency(r[4]), tag(r[5] === "已签署" ? "合同完成" : r[5]), r[6], `<button class="btn link" onclick="setPage('supplier-contract-detail')">查看详情</button>`,
    ]))}
  `;
}

function supplierContractDetailPage() {
  return `
    ${pageHead("合同详情", "供应商查看合同信息、签署状态和归档文件。", `<button class="btn" onclick="setPage('supplier-contracts')">返回合同列表</button><button class="btn primary">进入电子签署</button>`)}
    ${summary("CON-202605-013 · 品牌活动礼品采购合同", `${tag("待签署")}`, "采购公司：CDP集团 · 金额：70,914 · 货币：CNY")}
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>合同信息</h3></div>
        ${infoGrid([
          ["合同编号", "CON-202605-013"],
          ["关联RFQ", "RFQ-202605-001"],
          ["采购方", "CDP集团"],
          ["供应商", "上海博夷信息技术有限公司"],
          ["金额", "70,914"],
          ["货币", "CNY"],
          ["签署方式", "电子签"],
          ["当前状态", tag("待签署")],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>合同明细</h3></div>
        ${table(["物料/服务", "数量", "最终单价", "小计", "税率", "含税小计"], [
          ["礼盒包装服务", "3000", "¥4.50", "¥13,500", "6%", "¥14,310"],
        ])}
      </div>
    </div>
  `;
}

function supplierCertUploadPage() {
  return `
    ${pageHead("上传/更新证件", "供应商上传证件后进入采购员审核流程。")}
    <div class="notice" style="margin-bottom:14px;">当前表单由「表单设置 / 供应商证件上传表单」渲染。选择不同证件类型后，系统可按证件类型字段绑定显示扩展字段。</div>
    <div class="card">
      <div class="form-grid">
        ${selectField("证件类型", ["营业执照", "完税证明", "医疗机构执业许可证"])}
        ${field("证件文件", "完税证明_2025.pdf")}
        ${field("有效期开始", "2025-01-01")}
        ${field("有效期截止", "2026-05-20")}
      </div>
      <div class="notice" style="margin-top:14px;">支持 PDF、JPG、PNG，单个文件不超过 10MB。提交后证件状态为待审核。</div>
      <div class="sticky-actions"><button class="btn" onclick="setPage('supplier-company')">取消</button><button class="btn primary" onclick="setPage('supplier-company')">提交审核</button></div>
    </div>
  `;
}

function supplierContactEditPage() {
  return `
    ${pageHead("新增/编辑联系人", "维护供应商联系人，联系人不区分销售/财务类型，至少保留一个主要联系人。")}
    <div class="card">
      <div class="form-grid">
        ${field("联系人备注", "可填写对接范围，例如报价、合同、付款沟通")}
        ${field("姓名", "沈家伟")}
        ${field("职务", "总经理")}
        ${field("部门", "业务部")}
        ${field("手机号", "13321832330")}
        ${field("邮箱", "jerry.shen@iboye.com")}
        ${selectField("是否主要联系人", ["是", "否"])}
      </div>
      <div class="sticky-actions"><button class="btn" onclick="setPage(state.role === 'supplier' ? 'supplier-contacts' : 'supplier-detail')">取消</button><button class="btn primary" onclick="setPage(state.role === 'supplier' ? 'supplier-contacts' : 'supplier-detail')">保存联系人</button></div>
    </div>
  `;
}

function emailDetailPage() {
  return `
    ${pageHead("邮件详情", "查看邮件发送内容、状态和失败原因，支持失败重发。")}
    ${summary("报价截止提醒 · RFQ-202605-001", `${tag("失败", "red")}`, "收件人：高阳阳 / 1542533389@qq.com · 发送时间：2026-05-11 18:00")}
    <div class="grid cols-2">
      <div class="card">
        <div class="card-title"><h3>发送信息</h3></div>
        ${infoGrid([
          ["邮件类型", "QUOTE_DEADLINE_REMINDER"],
          ["关联 RFQ", "RFQ-202605-001"],
          ["供应商", "山西德利仁信息技术服务有限公司"],
          ["状态", tag("失败", "red")],
          ["失败原因", "SMTP 连接超时"],
          ["重试次数", "0"],
        ])}
      </div>
      <div class="card">
        <div class="card-title"><h3>邮件内容预览</h3></div>
        <p class="paragraph">【报价截止提醒】2026 年品牌活动礼品采购将于 2026-05-12 18:00 截止报价，请尽快登录采购管理平台提交报价。</p>
        <div class="sticky-actions"><button class="btn" onclick="setPage('email-logs')">返回</button><button class="btn primary">重新发送</button></div>
      </div>
    </div>
  `;
}

function emailLogsPage() {
  const f = currentFilters();
  const rows = [
    ["RFQ发布", "沈家伟", "jerry.shen@iboye.com", "RFQ-202605-001", "成功", "2026-05-06 09:01"],
    ["报价截止提醒", "高阳阳", "1542533389@qq.com", "RFQ-202605-001", "失败", "2026-05-11 18:00"],
    ["合同签署提醒", "程琳", "chenglin@example.com", "CON-202605-005", "待发送", "2026-05-12 09:00"],
  ].filter((r) =>
    textIncludes([r[0], r[3]], f.f0) &&
    exactMatch(r[4], f.f1) &&
    textIncludes([r[1], r[2]], f.f2) &&
    textIncludes([r[5]], f.f3)
  );
  return pageHead("邮件日志", "记录邀请、审核、询价、报价、审批等邮件发送状态。") + filters([
    "邮件类型 / 关联单据",
    { label: "发送状态", options: stateOptions.email },
    "收件人",
    "发送时间",
  ]) + tableWrap(["邮件类型", "收件人", "邮箱", "关联单据", "状态", "发送时间", "操作"], rows.map((r) => [
    r[0], r[1], r[2], r[3], tag(r[4]), r[5], r[4] === "失败" ? `<button class="btn link" onclick="setPage('email-detail')">查看</button><button class="btn link">重发</button>` : `<button class="btn link" onclick="setPage('email-detail')">查看</button>`,
  ]));
}

function table(heads, rows) {
  return `<div class="table-wrap"><div class="table-scroll-hint">左右滑动查看更多字段</div><table><thead><tr>${heads.map((h) => `<th>${h}</th>`).join("")}</tr></thead><tbody>${rows.map((r) => `<tr>${r.map((c) => `<td>${c}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`;
}

function tableWrap(heads, rows) {
  return table(heads, rows);
}

function currentFilters() {
  if (!state.filters[state.page]) state.filters[state.page] = {};
  return state.filters[state.page];
}

function setFilter(key, value) {
  currentFilters()[key] = value;
  render();
}

function resetFilters() {
  state.filters[state.page] = {};
  render();
}

function setPrForm(key, value) {
  state.prForm[key] = value;
  render();
}

function filterValue(key) {
  return currentFilters()[key] || "";
}

function textIncludes(values, query) {
  if (!query) return true;
  const normalized = String(query).trim().toLowerCase();
  if (!normalized) return true;
  return values.some((value) => String(value ?? "").toLowerCase().includes(normalized));
}

function exactMatch(value, selected) {
  return !selected || selected === "全部" || String(value) === selected;
}

function stripHtml(value) {
  return String(value ?? "").replace(/<[^>]*>/g, "");
}

function filters(items) {
  return `<div class="filters" style="--filter-count:${Math.min(items.length, 4)}"><div class="filter-fields">${items.map((item, i) => {
    const key = typeof item === "string" ? `f${i}` : item.key || `f${i}`;
    const value = filterValue(key);
    if (typeof item === "string") {
      return i === 0 || item.includes("/") || item.includes("名称") || item.includes("时间") || item.includes("联系人") || item.includes("部门") || item.includes("供应商") || item.includes("收件人")
        ? `<input class="input" placeholder="${item}" value="${value}" oninput="setFilter('${key}', this.value)" />`
        : `<select class="select" onchange="setFilter('${key}', this.value)"><option value="">${item}</option><option ${value === "全部" ? "selected" : ""}>全部</option></select>`;
    }
    return `<select class="select" onchange="setFilter('${key}', this.value)"><option value="">${item.label}</option><option ${value === "全部" ? "selected" : ""}>全部</option>${item.options.map((option) => `<option ${value === option ? "selected" : ""}>${option}</option>`).join("")}</select>`;
  }).join("")}</div><div class="filter-actions"><button class="btn primary" onclick="render()">查询</button><button class="btn ghost reset-btn" onclick="resetFilters()">重置</button></div></div>`;
}

function summary(title, tags, meta, actions = "") {
  return `<div class="summary"><div class="summary-top"><div><h2>${title}</h2><div class="summary-meta"><span>${tags}</span><span>${meta || ""}</span></div></div><div class="actions">${actions}</div></div></div>`;
}

function infoGrid(items) {
  return `<div class="info-grid">${items.map(([k, v]) => `<div class="info-item"><div class="k">${k}</div><div class="v">${v}</div></div>`).join("")}</div>`;
}

function helpTip(text) {
  return `<span class="help-tip" title="${text}">?</span>`;
}

function contractStatusHelp(status) {
  const map = {
    合同草稿: "合同申请已生成但尚未推送至 Worklife BPM。",
    外部审批推送中: "采购系统正在将合同申请推送至 Worklife BPM，等待 Worklife BPM 接收结果。",
    "Worklife BPM审批中": "Worklife BPM 已接收合同申请，正在进行外部审批，审批结果会回传采购系统。",
    "Worklife BPM审批通过": "Worklife BPM 已审批通过，采购系统可继续发起电子签或线下签署归档。",
    待签署: "合同审批已通过，等待选择电子签或上传线下签署归档。",
    电子签署中: "已调用电子签系统，等待采购方与供应商完成签署。",
    合同完成: "合同已签署归档，并已同步合同状态至采购申请单。",
  };
  return map[status] || "合同状态来自采购系统、Worklife BPM 或电子签系统的流转回写。";
}

function steps(items, current) {
  return `<div class="steps">${items.map((it, i) => `<div class="step ${i + 1 < current ? "done" : i + 1 === current ? "active" : ""}"><span class="step-dot">${i + 1}</span>${it}</div>${i < items.length - 1 ? '<span class="step-line"></span>' : ""}`).join("")}</div>`;
}

function field(label, value, disabled = false) {
  return `<div class="field"><label>${label}</label><input class="input" value="${value}" ${disabled ? "disabled" : ""} /></div>`;
}

function selectField(label, options) {
  return `<div class="field"><label>${label}</label><select class="select">${options.map((o) => `<option>${o}</option>`).join("")}</select></div>`;
}

function controlledSelectField(label, options, value, key) {
  return `<div class="field"><label>${label}</label><select class="select" onchange="setPrForm('${key}', this.value)">${options.map((o) => `<option ${value === o ? "selected" : ""}>${o}</option>`).join("")}</select></div>`;
}

function textareaField(label, value) {
  return `<div class="field full"><label>${label}</label><textarea class="textarea">${value}</textarea></div>`;
}

function timeline(title, desc) {
  return `<div class="timeline-item"><span class="timeline-dot"></span><div><strong>${title}</strong><span>${desc}</span></div></div>`;
}

function modalHtml() {
  if (!state.modal) return "";
  const templates = {
    notifications: ["消息提醒", "待审核 5 条、报价截止提醒 4 条、审批驳回 1 条。"],
    createSupplier: ["创建供应商", `<div class="form-grid">${field("供应商企业名称", "上海示例供应商有限公司")}${field("联系人姓名", "王强")}${field("联系人手机号", "13900000000")}${field("联系人邮箱", "wangqiang@example.com")}</div>`],
    inviteSent: ["邀请已发送", "供应商账号已创建，系统已发送包含登录地址、手机号和初始密码的邀请邮件；供应商状态更新为待进入。"],
    supplierSaved: ["已保存供应商", "供应商已创建成功，当前状态为创建成功。后续可在供应商详情中发送邀请、设为合作中或停用。"],
    supplierActivated: ["供应商状态已更新", "原型模拟：采购员已将供应商状态调整为合作中，系统记录操作人、操作时间、调整前后状态和备注。"],
    supplierDisabled: ["供应商已停用", "原型模拟：采购员已将供应商状态调整为已停用。该供应商不可登录、不可参与新询价，历史单据仍可查询。"],
    reviewInfo: ["信息变更审核", "供应商提交了银行账号和主力客户变更。请在真实系统中以左右对比表核对差异后通过或驳回。"],
    reviewCert: ["证件审核", "证件类型：完税证明。有效期：2025-01-01 至 2026-05-20。文件预览区域在高保真原型中可替换为 PDF 预览。"],
    reviewApproved: ["审核通过", "审核已通过，相关信息将正式生效，并通知供应商。"],
    reviewRejected: ["审核驳回", "审核已驳回，系统将通知供应商根据原因重新提交。"],
    returnQuote: ["退回报价", `<div class="field"><label>退回原因</label><textarea class="textarea">报价明细缺少交货周期说明，请补充后重新提交。</textarea></div>`],
    businessBudgetLine: ["新增预算明细", `<div class="notice">业务人员在 PR 创建时按行维护预算明细。保存后会回到 Budget 表格，用于计算 PR 预算总额。</div><div class="form-grid" style="margin-top:14px;">${field("产品/服务需求", "活动现场摄影摄像服务")}${field("采购数量", "1")}${selectField("单位", ["项", "套", "个", "天", "月"])}${field("采购单价", "12000")}${selectField("货币 Currency", ["CNY", "USD", "EUR"])}${field("总价", "12,000.00", true)}${textareaField("备注 Remark", "含摄影、短视频剪辑和活动照片精修。")}</div><div class="notice" style="margin-top:14px;">计算规则：总价 = 采购数量 × 采购单价；数量和单价必须大于 0；货币按行保存，PR 总预算按相同币种汇总展示。</div>`],
    businessPoLine: ["新增PO明细", `<div class="notice">PO 明细从合同明细带出，业务人员可按成本中心或销售订单拆分。保存后重新计算 PO 金额。</div><div class="form-grid" style="margin-top:14px;">${field("项目", "活动现场摄影摄像服务")}${field("数量", "1")}${selectField("单位", ["项", "套", "个", "天", "月"])}${field("单价", "12000")}${field("金额", "12,000.00", true)}${selectField("币种", ["CNY", "USD", "EUR"])}${field("成本中心", "MKT-2026-BRAND")}${field("销售订单号", "业务类PO时必填")}</div><div class="notice" style="margin-top:14px;">控制规则：费用类 PO 必填成本中心；业务类 PO 必填销售订单；金额合计不得超过合同可用金额。</div>`],
    buyerQuoteLine: ["报价内容明细行", `<div class="notice">该表单用于采购员在创建询价单时维护标准报价明细行。供应商报价页会默认带出这些明细行，供应商也可以新增自由报价行。</div><div class="form-grid" style="margin-top:14px;">${field("物料/服务名称", "定制帆布袋")}${field("数量", "3000")}${field("单价", "18.00")}${field("小计", "54,000.00", true)}${field("服务/交货天数", "18天")}${textareaField("备注", "带品牌 LOGO，含打样。")}</div><div class="notice" style="margin-top:14px;">计算规则：小计 = 数量 × 单价，保留 2 位小数；保存后刷新创建询价单的报价内容表。至少需要保留 1 行报价明细。</div>`],
    quoteLine: ["新增报价明细行", `<div class="notice">供应商可按采购员设置的报价内容填写，也可以补充自由报价项，例如配送费、安装费、折扣、增值服务等。</div><div class="form-grid" style="margin-top:14px;">${field("物料/服务名称", "上海市内配送费")}${field("数量", "1")}${field("单价", "0.00")}${field("小计", "0.00", true)}${field("服务/交货天数", "随货交付")}</div><div class="notice" style="margin-top:14px;">保存规则：小计由系统自动计算；报价提交前可编辑或删除明细行；报价提交后不可自行修改，除非采购员退回报价。</div>`],
    createFormField: ["新增字段", `<div class="form-grid">${field("字段编码", "certificate_license_no")}${field("字段名称", "许可证编号")}${selectField("控件类型", ["文本", "数字", "金额", "日期", "下拉选择", "附件上传", "多行文本"])}${selectField("数据类型", ["string", "number", "decimal", "date", "file", "boolean"])}${selectField("是否必填", ["是", "否"])}${field("校验规则", "最大50字，不允许重复")}${textareaField("选项/说明", "用于食品经营许可证、医疗机构执业许可证等证件类型的扩展字段。")}</div>`],
    createFormTemplate: ["创建表单", `<div class="form-grid">${field("表单名称", "供应商证件上传表单")}${selectField("业务对象", ["采购申请单 PurchaseRequest", "供应商信息 Supplier", "供应商证件 Certificate", "联系人 Contact"])}${textareaField("表单说明", "创建表单后同步生成该表单独立字段库，用于配置分组、排序、必填、显示条件和校验规则。")}</div>`],
    certTypeFieldConfig: ["配置证件类型字段", `<div class="notice">证件类型可绑定基础字段和扩展字段。供应商选择证件类型后，上传表单会动态显示对应字段。</div><div style="height:14px"></div>${table(["字段", "控件类型", "是否必填", "显示条件"], [["证件类型", "下拉选择", tag("必填", "red"), "全部证件"], ["证件文件", "附件上传", tag("必填", "red"), "全部证件"], ["有效期开始", "日期", tag("必填", "red"), "全部证件"], ["有效期截止", "日期", tag("必填", "red"), "全部证件"], ["许可证编号", "文本", tag("必填", "red"), "食品经营许可证"], ["经营项目", "多选", tag("必填", "red"), "食品经营许可证"]])}`],
    offlineQuote: ["代录供应商报价", `<div class="notice">单一来源、定向采购、续约不通知供应商，由采购员代录报价，并必须上传供应商报价附件。</div><div class="form-grid" style="margin-top:14px;">${field("供应商名称", "上海博夷信息技术有限公司")}${field("联系人", "沈家伟")}${field("电话/邮箱", "13321832330 / jerry.shen@iboye.com")}${field("物料/服务", "云监控订阅服务")}${field("数量", "12")}${field("单位", "月")}${field("未税单价", "5030")}${field("税率", "6%")}${field("报价附件", "上海博夷_报价邮件.pdf")}${textareaField("备注", "附件为供应商报价凭证，保存后进入统一核价流程。")}</div>`],
    mergePrBundle: ["创建PR合集", `<div class="notice">将多张已审批通过且尚未进入下游流程的 PR 合并为一张 PR 合集。合集创建后可像普通 PR 一样创建 RFQ，原始 PR 保留在合集明细中用于追溯。</div><div style="height:14px"></div>${table(["选择", "PR号", "部门", "内容摘要", "预算", "希望完成时间"], [[`<input type="checkbox" checked />`, "PR-202605-021", "行政部", "办公文具补充采购", "¥42,000", "2026-06-08"], [`<input type="checkbox" checked />`, "PR-202605-022", "市场部", "线下活动易耗品", "¥86,000", "2026-06-10"], [`<input type="checkbox" checked />`, "PR-202605-023", "运营部", "仓库包装辅助物料", "¥76,000", "2026-06-12"]])}<div class="form-grid" style="margin-top:14px;">${field("合集名称", "办公与活动物料采购合集")}${field("合集预算", "204000", true)}${field("采购员", "张伟", true)}${field("希望完成时间", "2026-06-12")}${textareaField("合并原因", "物料品类相近，可集中询价以提升供应商报价效率并统一后续核价审批。")}</div>`],
    removeBundlePr: ["移除合集明细", `<div class="notice">仅当 PR 合集尚未生成采购询价单时，才允许从合集详情中移除某条原始 PR。</div><div class="form-grid" style="margin-top:14px;">${field("移除PR", "PR-202605-024 / 办公区清洁用品", true)}${field("移除后合集预算", "20000", true)}${textareaField("移除原因", "该采购需求需要单独处理，不再参与本次合集询价。")}</div>`],
    priceAdjust: ["新增改价", `<div class="notice">采购员在核价中、采购经理在核价审批待处理时均可在最终中标明细中改价。改价只调整最终成交价快照，不覆盖供应商原始报价，也不是发起新一轮报价。</div><div class="form-grid" style="margin-top:14px;">${selectField("供应商", ["山西德利仁", "上海博夷"])}${selectField("报价明细", ["定制帆布袋 / 3000 个", "礼盒包装服务 / 3000 套", "新增服务项"])}${field("单价", "17.50", true)}${field("小计", "52,500.00", true)}${field("调整后最终成交价", "17.20")}${field("调整后最终小计", "51,600.00", true)}${textareaField("改价原因", "谈判后供应商确认下调单价，交付周期和付款条件不变。")}</div><div class="notice" style="margin-top:14px;">保存后系统记录原价、新价、原因、操作人和时间；审批与合同使用调整后的最终成交价。</div>`],
    awardLine: ["添加最终中标明细", `<div class="notice">添加行用于补充进入审批、合同和 PO 的最终中标明细。新增行必须绑定当前最终中标供应商，并记录来源和原因。</div><div class="form-grid" style="margin-top:14px;">${selectField("最终中标供应商", ["山西德利仁信息技术服务有限公司", "上海博夷信息技术有限公司", "上海乐尔芙农业科技有限公司"])}${field("物料/服务名称", "补充包装打样服务")}${field("数量", "1")}${field("单位", "项")}${field("单价", "1200")}${field("小计", "1200", true)}${field("税率", "6%")}${field("含税小计", "1272", true)}${field("服务/交货天数", "5")}${selectField("来源", ["供应商报价附件", "采购员补充归集", "采购经理审批调整"])}${textareaField("添加原因", "报价附件中包含该服务项，需补充进入最终中标明细。")}</div>`],
    priceReviewConfirm: ["提交核价审批确认", `<div class="notice">请确认本次提交给采购经理审批的核价结果。该弹窗只做提交确认，不展示审批摘要预览或审批前检查模块。</div><div class="form-grid" style="margin-top:14px;">${field("RFQ编号", "RFQ-202605-001", true)}${field("核价含税总额", "70,914.00", true)}${field("最终中标供应商", "山西德利仁信息技术服务有限公司", true)}${field("最终成交价说明", "整单中标；最终中标明细包含定制帆布袋和礼盒包装服务两行", true)}${field("审批人", "采购经理 周敏", true)}${textareaField("提交说明", "报价明细、最终成交价和附件已确认，提交采购经理审批。")}</div>`],
    proxyQuote: ["代询价", "进入代询价页面：采购员代供应商填写报价，并必须上传供应商报价附件。"],
    newRound: ["开启新一轮报价", `<div class="form-grid">${field("新报价开始时间", "2026-05-14 09:00")}${field("新报价结束时间", "2026-05-16 18:00")}${field("新开标时间", "2026-05-17 10:00")}</div>`],
    rejectApproval: ["审批驳回", `<div class="field"><label>驳回原因</label><textarea class="textarea">请补充供应商选择理由和拆分中标依据。</textarea></div>`],
    approved: ["审批通过", "RFQ 核价审批已通过。采购员将在采购员端「合同管理」列表顶部点击「创建合同」，选择该 RFQ 后生成合同申请。"],
    oaPending: ["Worklife BPM审批进度", "Worklife BPM 合同审批已发起，审批单号 BPM-CON-202605-001。原型中可点击“模拟Worklife BPM审批通过”进入下一步。"],
    esignPending: ["电子签进度", "电子签署流程已发起，等待采购方和供应商完成签署。原型中可点击“模拟签署完成并同步PR”。"],
    offlineArchive: ["线下签署文档上传归档", `<div class="notice">Worklife BPM审批通过后，如选择线下签署，采购员需要上传双方签署完成的合同文件并录入线下合同编号。确认后合同状态变为「合同完成」，并回写 PR 合同状态。</div><div class="form-grid" style="margin-top:14px;">${field("线下合同编号", "OFF-CON-202605-013")}${field("签署完成日期", "2026-05-18")}${field("采购方签署人", "张伟")}${field("供应商签署人", "沈家伟")}${field("归档文件", "品牌活动礼品采购合同_双方盖章版.pdf")}${selectField("文件校验", ["已核对双方盖章签字", "待补充签署页"])}${textareaField("归档备注", "合同已完成线下签署，扫描件上传归档，原件由采购部留存。")}</div>`],
    quoteSubmitted: ["报价已提交", "你的报价已提交成功。MVP 规则下报价截止前也不可自行修改；如采购员驳回报价，可重新提交。"],
  };
  const [title, body] = templates[state.modal];
  return `
    <div class="modal-backdrop" onclick="closeModal()">
      <div class="modal" onclick="event.stopPropagation()">
        <h3>${title}</h3>
        ${body.startsWith("<") ? body : `<p>${body}</p>`}
        <div class="actions" style="justify-content:flex-end;margin-top:16px;">
          <button class="btn" onclick="closeModal()">取消</button>
          <button class="btn primary" onclick="confirmModal()">确认</button>
        </div>
      </div>
    </div>
  `;
}

function toastHtml() {
  if (!state.toast) return "";
  return `<div class="toast" role="status">${state.toast}</div>`;
}

function renderPage() {
  const routes = {
    "buyer-dashboard": buyerDashboard,
    suppliers: suppliersPage,
    "supplier-detail": supplierDetail,
    "supplier-create": supplierCreatePage,
    "supplier-edit": supplierEditPage,
    "buyer-cert-upload": buyerCertUploadPage,
    reviews: reviewsPage,
    "review-info-detail": reviewInfoDetailPage,
    "review-cert-detail": reviewCertDetailPage,
    "purchase-requests": purchaseRequestsPage,
    "pr-detail": prDetailPage,
    rfqs: rfqsPage,
    "create-rfq": createRfqPage,
    "offline-sourcing": offlineSourcingPage,
    "rfq-edit": rfqEditPage,
    "rfq-detail": rfqDetailPage,
    "quote-detail": quoteDetailPage,
    "proxy-quote": proxyQuotePage,
    "new-round": newRoundPage,
    "bid-summary": bidSummaryPage,
    "price-review": priceReviewPage,
    "price-review-quotes": priceReviewQuotesPage,
    "approval-status": approvalStatusPage,
    contracts: contractsPage,
    "contract-create": contractCreatePage,
    "contract-detail": contractDetailPage,
    pos: poListPage,
    "po-detail": poDetailPage,
    "admin-contract-detail": contractDetailPage,
    "admin-dashboard": adminDashboard,
    "admin-approval": reviewsPage,
    "admin-approval-detail": () => adminApprovalPage(false),
    "admin-data-settings": adminDataSettingsPage,
    "admin-buyers": () => adminListPage("采购员账号管理"),
    "admin-field-settings": fieldSettingsPage,
    "admin-field-library-detail": fieldLibraryDetailPage,
    "admin-field-create": fieldCreatePage,
    "admin-field-detail": fieldDetailPage,
    "admin-form-settings": formSettingsPage,
    "admin-form-detail": formDetailPage,
    "admin-form-create": formCreatePage,
    "admin-form-config": formConfigPage,
    "admin-form-preview": formPreviewPage,
    "admin-rfqs": adminRfqQueryPage,
    "admin-contracts": adminContractsPage,
    "admin-pos": poListPage,
    "admin-report-executive": adminReportExecutivePage,
    "admin-report-spend": adminReportSpendPage,
    "admin-report-process": adminReportProcessPage,
    "admin-report-suppliers": adminReportSuppliersPage,
    "admin-report-risks": adminReportRisksPage,
    "business-prs": businessPrsPage,
    "business-pr-create": businessPrCreatePage,
    "business-pr-detail": businessPrDetailPage,
    "business-contracts": businessContractsPage,
    "business-pos": businessPosPage,
    "business-po-create": businessPoCreatePage,
    "supplier-dashboard": supplierCompanyPage,
    "supplier-company": supplierCompanyPage,
    "supplier-certs": () => supplierSimpleList("supplier-certs"),
    "supplier-contacts": () => supplierSimpleList("supplier-contacts"),
    "supplier-rfqs": () => supplierSimpleList("supplier-rfqs"),
    "supplier-rfq-detail": supplierRfqDetailPage,
    "supplier-quote": supplierQuotePage,
    "supplier-contracts": supplierContractsPage,
    "supplier-contract-detail": supplierContractDetailPage,
    "supplier-cert-upload": supplierCertUploadPage,
    "supplier-contact-edit": supplierContactEditPage,
    "email-logs": emailLogsPage,
    "email-detail": emailDetailPage,
  };
  return (routes[state.page] || buyerDashboard)();
}

function render() {
  document.getElementById("app").innerHTML = state.authed ? appShell(renderPage()) : loginPage();
}

window.setPage = setPage;
window.setRole = setRole;
window.openModal = openModal;
window.closeModal = closeModal;
window.confirmModal = confirmModal;
window.goBack = goBack;
window.login = login;
window.logout = logout;
window.selectPr = selectPr;
window.selectRfq = selectRfq;
window.selectContract = selectContract;
window.selectPo = selectPo;
window.setContractStatus = setContractStatus;
window.selectFieldLibrary = selectFieldLibrary;
window.selectFormTemplate = selectFormTemplate;
window.setFilter = setFilter;
window.resetFilters = resetFilters;
window.state = state;

if (typeof window !== "undefined" && window.addEventListener) {
  window.addEventListener("hashchange", () => {
    if (!suppressHashSync) applyHashRoute();
  });
}

if (!applyHashRoute()) render();
