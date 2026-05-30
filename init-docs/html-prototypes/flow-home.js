(function () {
  const FLOW_STORAGE_KEY = "__procurementFlowScenario";

  const flows = [
    {
      id: "business-pr-create",
      title: "业务人员发起采购申请",
      owner: "业务人员",
      role: "business",
      page: "business-pr-create",
      data: "测试PR：PR-202605-026 / 新品发布会物料与搭建服务",
      goal: "演示业务人员在采购系统创建采购申请单，提交第三方审批，并跟踪审批、采购员分配和后续采购进展。",
      preset: { selectedPr: "PR-202605-026" },
      steps: [
        ["创建采购申请单", "填写申请人、部门、需求说明、成本中心、预算明细；演示唯一供应商和业务成本类采购的条件字段"],
        ["提交第三方审批", "采购系统发起审批请求，审批系统回传审批状态和分配采购员"],
        ["查看我的采购申请", "业务人员只能查看本人发起的 PR"],
        ["进入跟踪详情", "按阶段查看审批、分配采购员、合同、PO和付款状态；业务人员端不展示RFQ"],
      ],
    },
    {
      id: "supplier-create",
      title: "供应商创建与邀请",
      owner: "采购员",
      role: "buyer",
      page: "supplier-create",
      data: "示例供应商：上海示例供应商有限公司",
      goal: "演示采购员创建供应商、维护联系人，并手动发送供应商门户邀请邮件。",
      steps: [
        ["采购员进入供应商管理", "查看供应商列表和合作状态"],
        ["点击创建供应商", "填写企业基础信息、分类、合作属性"],
        ["进入供应商详情", "采购员或采购经理可直接新增联系人"],
        ["发送邀请邮件", "联系人收到登录供应商门户的邀请"],
      ],
    },
    {
      id: "buyer-rfq-setup",
      title: "采购员创建询价单",
      owner: "采购员",
      role: "buyer",
      page: "create-rfq",
      data: "测试PR：PR-202605-001",
      goal: "演示从采购申请单创建 RFQ，通过采购策略区分询比价、单一来源、定向采购和续约。",
      preset: { selectedPr: "PR-202605-001", selectedRfq: "RFQ-202605-001", createStep: 1 },
      steps: [
        ["选择采购申请单", "从已审批并分配给采购员的 PR 或 PR 合集发起询价"],
        ["填写项目基本信息", "CDP公司抬头从 PR 带出，报价单模板仅保留下载"],
        ["配置报价安排", "设置报价截止时间、报价内容和采购策略"],
        ["选择参与供应商", "询比价可多供应商并选通知联系人；其他策略仅单供应商"],
        ["确认发布或代录", "询比价发布并通知供应商；单一来源/定向采购/续约进入报价中并由采购员代录报价"],
      ],
    },
    {
      id: "supplier-quote",
      title: "供应商报价",
      owner: "供应商",
      role: "supplier",
      page: "supplier-rfqs",
      data: "测试RFQ：RFQ-202605-003",
      goal: "演示供应商查看询价详情、提交报价；报价被退回时可查看退回原因。企业信息维护与询价报价各自独立。",
      steps: [
        ["查看询价列表", "从供应商端菜单进入询价列表，不在列表内引导完善企业信息"],
        ["进入我的询价单", "查看待报价、已提交、已退回的询价单"],
        ["打开询价单详情", "查看采购需求、报价规则、退回原因"],
        ["提交报价", "填写固定报价字段并上传报价附件"],
      ],
    },
    {
      id: "proxy-quote",
      title: "采购员代录报价",
      owner: "采购员",
      role: "buyer",
      page: "offline-sourcing",
      data: "测试RFQ：RFQ-202605-012 / 云监控订阅服务",
      goal: "演示单一来源、定向采购、续约策略下，采购员不通知供应商，代录供应商报价并上传报价附件。",
      preset: { selectedPr: "PR-202605-012", selectedRfq: "RFQ-202605-012" },
      steps: [
        ["创建询价单", "采购策略选择单一来源、定向采购或续约"],
        ["选择一家供应商", "系统不发送询价邮件，不生成供应商端报价待办"],
        ["代录报价", "录入税率、币种、账期、付款方式和报价明细"],
        ["上传报价附件", "上传供应商报价单、邮件截图或盖章报价文件"],
        ["进入核价", "代录报价保存后进入统一核价流程"],
      ],
    },
    {
      id: "price-review",
      title: "核价与审批",
      owner: "采购员 / 采购经理",
      role: "buyer",
      page: "price-review",
      data: "测试RFQ：RFQ-202605-001",
      goal: "演示报价截止后的核价分析、单项改价记录、提交核价审批，以及采购经理在审批中心处理。",
      preset: { selectedPr: "PR-202605-001", selectedRfq: "RFQ-202605-001", rfqStatus: "核价中" },
      steps: [
        ["核价总览", "查看供应商报价、预算差异和核价结论"],
        ["报价复核", "在供应商报价列表中对具体明细执行新增改价"],
        ["提交审批", "点击提交后先弹出提交确认，再提交采购经理审批核价结果"],
        ["经理审批中心", "核价审批作为审批中心的一类任务处理"],
      ],
    },
    {
      id: "contract-flow",
      title: "合同创建、Worklife BPM审批与签署",
      owner: "采购员",
      role: "buyer",
      page: "contract-create",
      data: "测试RFQ：RFQ-202605-001，合同：CON-202605-002",
      goal: "演示合同管理只展示已创建合同，通过已审批完成RFQ创建合同申请，并进入外部审批推送、签署归档。",
      preset: { selectedPr: "PR-202605-001", selectedRfq: "RFQ-202605-001", selectedContract: "CON-202605-002" },
      steps: [
        ["合同管理", "点击创建合同，选择已审批完成RFQ"],
        ["填写合同数据", "金额和货币拆分维护；超过PR预算时提示风险，不做系统硬阻断"],
        ["外部审批推送", "展示Worklife BPM审批流转中间状态"],
        ["签署归档", "支持电子签；线下签署需上传双方签署文件并手动归档"],
      ],
    },
    {
      id: "po-payment",
      title: "业务人员创建PO与付款闭环",
      owner: "业务人员",
      role: "business",
      page: "business-pos",
      data: "测试PR：PR-202605-018，PO：PO-202605-005",
      goal: "演示合同完成后由业务人员创建 PO、提交第三方审批、同步台账并查看付款回传。",
      preset: { selectedPr: "PR-202605-018", selectedPo: "PO-202605-005", selectedContract: "CON-202605-010" },
      steps: [
        ["进入我的PO", "查看本人 PR 关联的 PO 和付款状态"],
        ["选择已完成合同", "从可创建 PO 的合同进入创建 PO 页面"],
        ["填写PO信息", "维护费用类/业务类、成本中心或销售订单、PO明细和附件"],
        ["提交审批", "PO进入第三方审批，审批通过后同步台账"],
        ["付款回传", "台账系统付款完成后回传采购系统"],
      ],
    },
    {
      id: "data-settings",
      title: "数据设置：字段库与表单配置",
      owner: "采购经理",
      role: "admin",
      page: "admin-data-settings",
      data: "字段库：采购申请单 / 供应商企业信息 / 证件 / 联系人",
      goal: "演示采购经理通过数据设置维护采购员账号、不同业务表单的独立字段库和表单配置。",
      preset: { selectedFieldLibrary: "pr", selectedFormTemplate: "pr" },
      steps: [
        ["进入数据设置", "采购员账号、字段设置、表单设置作为子菜单"],
        ["选择字段库", "不同表单维护独立字段库"],
        ["创建或编辑字段", "维护字段编码、类型、必填、选项和校验"],
        ["配置表单", "将字段组装为业务表单并保存生效"],
      ],
    },
  ];

  function roleLabel(role) {
    return role === "admin" ? "采购经理端" : role === "supplier" ? "供应商端" : role === "business" ? "业务人员端" : "采购员端";
  }

  function flowHomeMarkup() {
    return `
      <main class="flow-home">
        <section class="flow-hero">
          <div>
            <p class="eyebrow">流程场景高保真原型</p>
            <h1>按业务流程进入采购系统演示</h1>
            <p>保留原三角色原型不变，本入口把业务部门最容易测试的链路拆成独立场景。每个场景都带推荐测试数据，进入后可继续使用页面内按钮完成流转。</p>
          </div>
          <div class="flow-hero-panel">
            <strong>建议演示顺序</strong>
            <span>业务发起PR → 供应商创建 → 创建询价单 → 供应商报价/代录报价 → 核价审批 → 合同 → PO付款</span>
          </div>
        </section>

        <section class="flow-grid">
          ${flows.map((flow, index) => `
            <article class="flow-card">
              <div class="flow-card-head">
                <span class="flow-index">${String(index + 1).padStart(2, "0")}</span>
                <span class="role-pill">${roleLabel(flow.role)}</span>
              </div>
              <h2>${flow.title}</h2>
              <p>${flow.goal}</p>
              <div class="flow-data">${flow.data}</div>
              <ol>
                ${flow.steps.map((step) => `<li><strong>${step[0]}</strong><span>${step[1]}</span></li>`).join("")}
              </ol>
              <div class="flow-card-actions">
                <button class="btn primary" onclick="startFlow('${flow.id}')">进入流程</button>
                <button class="btn" onclick="previewFlow('${flow.id}')">查看步骤</button>
              </div>
            </article>
          `).join("")}
        </section>
      </main>
    `;
  }

  function previewMarkup(flow) {
    return `
      <main class="flow-home">
        <button class="btn" onclick="showFlowHome()">返回流程首页</button>
        <section class="flow-detail-hero">
          <span class="role-pill">${roleLabel(flow.role)}</span>
          <h1>${flow.title}</h1>
          <p>${flow.goal}</p>
          <div class="flow-data">${flow.data}</div>
        </section>
        <section class="flow-timeline">
          ${flow.steps.map((step, index) => `
            <div class="flow-timeline-item">
              <span>${index + 1}</span>
              <div>
                <h3>${step[0]}</h3>
                <p>${step[1]}</p>
              </div>
            </div>
          `).join("")}
        </section>
        <div class="flow-bottom-actions">
          <button class="btn primary" onclick="startFlow('${flow.id}')">进入此流程</button>
        </div>
      </main>
    `;
  }

  function renderFlow(markup) {
    const app = document.getElementById("app");
    if (!app) return;
    app.innerHTML = markup;
  }

  function applyPreset(flow) {
    if (!window.state) return;
    window.state.authed = true;
    window.state.role = flow.role;
    window.state.page = flow.page;
    window.state.pageHistory = [];
    window.state.modal = null;
    window.state.toast = null;
    window.state.createStep = 1;
    window.state.prTab = "overview";
    window.state.reviewTab = "todo";
    Object.assign(window.state, flow.preset || {});
  }

  function injectFlowReturn() {
    window.setTimeout(() => {
      if (!window.state || !window.state.authed) return;
      if (document.querySelector(".flow-home")) return;
      if (document.getElementById("flow-home-return")) return;
      const button = document.createElement("button");
      button.id = "flow-home-return";
      button.className = "flow-home-return";
      button.type = "button";
      button.textContent = "流程首页";
      button.onclick = window.showFlowHome;
      document.body.appendChild(button);
    }, 0);
  }

  window.showFlowHome = function showFlowHome() {
    if (window.state) {
      window.state.authed = false;
      window.state.pageHistory = [];
    }
    const existing = document.getElementById("flow-home-return");
    if (existing) existing.remove();
    if (window.history && window.location) {
      window.history.replaceState(null, "", `${window.location.pathname}#/flow/home`);
    }
    renderFlow(flowHomeMarkup());
  };

  window.previewFlow = function previewFlow(id) {
    const flow = flows.find((item) => item.id === id);
    if (!flow) return;
    const existing = document.getElementById("flow-home-return");
    if (existing) existing.remove();
    renderFlow(previewMarkup(flow));
  };

  window.startFlow = function startFlow(id) {
    const flow = flows.find((item) => item.id === id);
    if (!flow) return;
    window.localStorage && window.localStorage.setItem(FLOW_STORAGE_KEY, id);
    applyPreset(flow);
    if (typeof window.setPage === "function") {
      window.setPage(flow.page, { replace: true });
    } else if (typeof window.render === "function") {
      window.render();
    }
    injectFlowReturn();
  };

  const originalSetPage = window.setPage;
  if (typeof originalSetPage === "function") {
    window.setPage = function wrappedSetPage(page, options) {
      originalSetPage(page, options);
      injectFlowReturn();
    };
  }

  const originalSetRole = window.setRole;
  if (typeof originalSetRole === "function") {
    window.setRole = function wrappedSetRole(role, options) {
      originalSetRole(role, options);
      injectFlowReturn();
    };
  }

  const hash = window.location.hash || "";
  if (!hash || hash.startsWith("#/flow")) {
    window.showFlowHome();
  } else {
    injectFlowReturn();
  }
})();
