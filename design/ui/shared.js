/* Motor de las maquetas de TeLoLeo. No es la app. */
(function () {
  "use strict";

  var APPS = [
    { id: "wa", name: "WhatsApp", pkg: "com.whatsapp", color: "#128C7E" },
    { id: "tg", name: "Telegram", pkg: "org.telegram.messenger", color: "#2AABEE" },
    { id: "gm", name: "Gmail", pkg: "com.google.android.gm", color: "#C5221F" },
    { id: "sms", name: "Mensajes", pkg: "com.google.android.apps.messaging", color: "#1A73E8" },
    { id: "ig", name: "Instagram", pkg: "com.instagram.android", color: "#C13584" },
    { id: "maps", name: "Maps", pkg: "com.google.android.apps.maps", color: "#34A853" },
    { id: "cal", name: "Calendario", pkg: "com.google.android.calendar", color: "#4285F4" },
    { id: "bank", name: "Banco", pkg: "com.banco.app", color: "#0B3D2E" },
    { id: "slack", name: "Slack", pkg: "com.Slack", color: "#4A154B" },
    { id: "signal", name: "Signal", pkg: "org.thoughtcrime.securesms", color: "#3A76F0" },
    { id: "spot", name: "Spotify", pkg: "com.spotify.music", color: "#1DB954" },
    { id: "chrome", name: "Chrome", pkg: "com.android.chrome", color: "#F4B400" },
    { id: "yt", name: "YouTube", pkg: "com.google.android.youtube", color: "#FF0033" },
    { id: "photos", name: "Fotos", pkg: "com.google.android.apps.photos", color: "#F9AB00" },
    { id: "drive", name: "Drive", pkg: "com.google.android.apps.docs", color: "#0F9D58" },
    { id: "phone", name: "Teléfono", pkg: "com.google.android.dialer", color: "#1A73E8" },
    { id: "clock", name: "Reloj", pkg: "com.google.android.deskclock", color: "#5F6368" },
    { id: "wab", name: "WhatsApp Business", pkg: "com.whatsapp.w4b", color: "#25D366" }
  ];

  var BRANDS = {
    samsung: ["Samsung", "En Ajustes, Batería, deja TeLoLeo fuera de las apps en suspensión. En la ficha de TeLoLeo, Batería, elige Sin restricciones."],
    xiaomi: ["Xiaomi", "En Ajustes, busca TeLoLeo, Batería, y elige Sin restricciones. Activa el inicio automático si el teléfono lo muestra."],
    transsion: ["Transsion", "En Ajustes, busca TeLoLeo, Batería, Sin restricciones. Si hay inicio automático o apps protegidas, incluye TeLoLeo."],
    huawei: ["Huawei", "En Ajustes, Batería, Inicio de aplicaciones, busca TeLoLeo y permite inicio automático, inicio secundario y ejecución en segundo plano."],
    oppo: ["Oppo y familia", "En Ajustes, Batería, TeLoLeo, Sin restricciones. Activa el inicio automático si aparece."],
    generic: ["Otra marca", "En Ajustes, Aplicaciones, TeLoLeo, Batería, elige Sin restricciones. Así el teléfono no corta la lectura al rato."]
  };

  var COPY = {
    name: "TeLoLeo",
    subtitle: "En voz alta, solo de las apps que elijas.",
    listenerOn: "La lectura está activa.",
    listenerOff: "Falta activar el acceso a las notificaciones.",
    listenerOffDetail: "Solo se leen las apps que marques. El texto no sale del teléfono.",
    turnOff: "Para apagarla, desactiva TeLoLeo en el acceso a notificaciones.",
    enable: "Activar lectura",
    manage: "Abrir ajuste del sistema",
    silence: "Callar 2 min",
    redact: "Ocultar códigos de un solo uso",
    redactDetail: "Un código como 482913 se dice «oculto». Viene activado.",
    search: "Buscar app",
    clear: "Borrar búsqueda",
    loading: "Buscando las apps del teléfono…",
    noHits: "Ninguna app coincide.",
    silenceDefault: "Por defecto, silencio. Marca las apps que quieres oír.",
    sectionOn: "Se leen",
    sectionOff: "Otras apps",
    saved: "Guardada. Se lee con Pro.",
    ownPhrase: "Frase propia",
    template: "Plantilla",
    reading: "Lectura",
    readingNote: "Horario, auriculares, plantilla, filtros, agitar, velocidad y volumen son de Pro.",
    quiet: "Horario de silencio",
    from: "Desde",
    until: "Hasta",
    headphones: "Solo con auriculares o Bluetooth",
    shake: "Agitar para callar",
    shakeDetail: "Corta la voz durante 2 minutos.",
    rate: "Velocidad",
    volume: "Volumen de la voz",
    filters: "Filtro de palabras",
    filtersDetail: "Si el aviso contiene alguna, no se lee.",
    filtersHint: "promoción, grupo",
    phraseTitle: "Plantilla de la frase",
    templateHelp: "Usa {app}, {remitente} y {texto}. Vacía, se lee aplicación, título y texto. La plantilla de una app manda sobre la general.",
    templateAlias: "{titulo} es el mismo hueco que {remitente}.",
    save: "Guardar",
    cancel: "Cancelar",
    notNow: "Ahora no",
    back: "Volver",
    withPro: "Ver Pro",
    battery: "Para que no se calle en segundo plano",
    disclosureTitle: "TeLoLeo leerá notificaciones en voz alta",
    disclosureNext: "El siguiente paso es el ajuste de Android, no una pantalla de TeLoLeo.",
    disclosureOk: "Entendido, abrir ajustes",
    limitTitle: "3 apps en la versión gratis",
    limitBody: "Pro lee todas las que elijas, con un solo pago. No es una suscripción.",
    limitOrder: "Esta no se añade.",
    seePro: "Ver Pro",
    proTitle: "Pro",
    proIntro: "Un solo pago, en este teléfono. No es una suscripción.",
    proActive: "Pro está activo en este teléfono.",
    proPending: "La compra está pendiente. Pro se activa cuando Play la confirma.",
    proFree: "La versión gratis lee 3 apps, sin límite de lecturas, con la voz del sistema y los códigos ocultos.",
    restore: "Restaurar compra",
    privacyTitle: "Política de privacidad",
    privacyIntro: "TeLoLeo lee en voz alta las notificaciones de las apps que tú eliges.",
    privacyUrl: "La página pública dice lo mismo. Esa URL es la que va en la ficha de Play.",
    add: "Añadir"
  };

  var DISCLOSURE = [
    "TeLoLeo lee el título y el texto de las notificaciones de las apps que tú marques, y los dice con la voz del teléfono.",
    "Eso ocurre en el dispositivo. El texto no sale del teléfono: no se envía, no se guarda como historial y no se vende.",
    "Puedes apagarlo cuando quieras: quita las apps de la lista, o desactiva TeLoLeo en el acceso a notificaciones del sistema.",
    "Los códigos de un solo uso se ocultan al hablar, salvo que lo cambies."
  ];

  var PRIVACY = [
    ["Qué se lee", "El título y el texto que la otra app puso en el aviso. Si esa app oculta la vista previa, solo se dice su nombre."],
    ["Dónde se procesa", "En este teléfono. El texto no sale del teléfono: no se envía, no se guarda como historial y no se vende. No hay servidor ni anuncios."],
    ["Qué se guarda en el teléfono", "La lista de apps que marcaste, si compraste Pro y tus ajustes de lectura. No se copia a la nube."],
    ["Códigos", "Los códigos de un solo uso se sustituyen por «oculto» al hablar. Puedes desactivar eso en la pantalla principal."],
    ["Cómo se apaga", "Quita las apps de la lista, o desactiva TeLoLeo en Ajustes, Acceso a las notificaciones."],
    ["Seguridad de los datos", "En la ficha de Play: no se recopilan datos y no se comparten. TeLoLeo no recopila ni comparte datos. El texto del aviso se usa en memoria para decirlo y se descarta. La lista de apps y los ajustes se quedan en este teléfono. No hay anuncios ni identificador publicitario. No hay copia en la nube. Borrar los datos de TeLoLeo, o desinstalarla, vacía esa lista."],
    ["Pro", "El pago único lo cobra Google Play en el teléfono. Reinstalar la app restaura Pro con la misma cuenta de Play. No hay cuenta de TeLoLeo."],
    ["Menores", "TeLoLeo no está dirigida a menores."]
  ];

  var FEATURES = [
    ["Apps ilimitadas", "Se leen todas las que marques, no solo 3."],
    ["Horario de silencio", "Por ejemplo de 22:00 a 07:00."],
    ["Solo auriculares o Bluetooth", "Si el sonido va por el altavoz, no habla."],
    ["Plantilla de la frase", "Tú decides cómo se arma lo que se oye."],
    ["Filtro de palabras", "Si el aviso contiene alguna, no se lee."],
    ["Agitar para callar", "Corta la voz durante 2 minutos."],
    ["Velocidad y volumen propios", "Aparte del volumen general del teléfono."]
  ];

  var CATALOG = [
    { group: "Inicio", id: "home-off", label: "Lectura apagada", patch: { view: "home", listener: false, speakingNow: false, sheet: null, pro: false } },
    { group: "Inicio", id: "home-on", label: "Leyendo", patch: { view: "home", listener: true, speakingNow: false, sheet: null } },
    { group: "Inicio", id: "home-speaking", label: "Hablando ahora", patch: { view: "home", listener: true, speakingNow: true, sheet: null } },
    { group: "Apps", id: "apps", label: "Lista", patch: { view: "apps", listener: true, query: "", loading: false, sheet: null } },
    { group: "Apps", id: "apps-empty", label: "Sin marcas", patch: { view: "apps", selected: [], query: "", loading: false, listener: false } },
    { group: "Apps", id: "apps-search", label: "Búsqueda", patch: { view: "apps", query: "map", loading: false, listener: true } },
    { group: "Apps", id: "apps-none", label: "Sin resultados", patch: { view: "apps", query: "zzzz", loading: false, listener: true } },
    { group: "Apps", id: "apps-loading", label: "Cargando", patch: { view: "apps", loading: true, query: "", listener: true } },
    { group: "Apps", id: "apps-extra", label: "Guardada hasta Pro", patch: { view: "apps", pro: false, selected: ["wa", "tg", "gm", "signal"], listener: true } },
    { group: "Lectura", id: "lectura", label: "Bloqueada", patch: { view: "lectura", pro: false, sheet: null } },
    { group: "Lectura", id: "lectura-pro", label: "Con Pro", patch: { view: "lectura", pro: true, quiet: true, quietFrom: 22, quietTo: 7, headphones: true, shake: true, rate: 1.1, volume: 0.8, filters: ["promoción", "grupo"], template: "{app}. {texto}", appTemplates: { wa: "{remitente}: {texto}" }, sheet: null, listener: true } },
    { group: "Lectura", id: "plantilla", label: "Plantilla general", patch: { view: "lectura", pro: true, sheet: "template", templateTarget: null, draft: "{app}. {remitente}. {texto}" } },
    { group: "Lectura", id: "plantilla-app", label: "Plantilla de una app", patch: { view: "apps", pro: true, listener: true, sheet: "template", templateTarget: "wa", draft: "{remitente}: {texto}" } },
    { group: "Avisos", id: "limite", label: "Límite de 3 apps", patch: { view: "apps", sheet: "limit", pro: false, listener: true } },
    { group: "Avisos", id: "divulgacion", label: "Divulgación", patch: { view: "home", listener: false, sheet: "disclosure" } },
    { group: "Avisos", id: "bateria", label: "Batería", patch: { view: "home", sheet: "battery", listener: true } },
    { group: "Avisos", id: "sistema", label: "Ajuste del sistema", patch: { view: "sistema", listener: false, sheet: null } },
    { group: "Pro", id: "pro", label: "Comprar", patch: { view: "pro", pro: false, offer: "buy", sheet: null } },
    { group: "Pro", id: "pro-price", label: "Con precio", patch: { view: "pro", pro: false, offer: "price", sheet: null } },
    { group: "Pro", id: "pro-pending", label: "Pendiente", patch: { view: "pro", pro: false, offer: "pending", sheet: null } },
    { group: "Pro", id: "pro-unavail", label: "Play no disponible", patch: { view: "pro", pro: false, offer: "unavailable", sheet: null } },
    { group: "Pro", id: "pro-missing", label: "Producto ausente", patch: { view: "pro", pro: false, offer: "missing", sheet: null } },
    { group: "Pro", id: "pro-failed", label: "Compra fallida", patch: { view: "pro", pro: false, offer: "failed", sheet: null } },
    { group: "Pro", id: "pro-active", label: "Activo", patch: { view: "pro", pro: true, offer: "active", sheet: null } },
    { group: "Privacidad", id: "privacidad", label: "Política", patch: { view: "privacy", sheet: null } }
  ];

  function esc(value) {
    return String(value == null ? "" : value).replace(/[&<>"']/g, function (ch) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[ch];
    });
  }

  function groups(transform) {
    var items = CATALOG.map(function (item) {
      var copy = structuredClone(item);
      return transform ? transform(copy) : copy;
    });
    var order = [];
    var map = new Map();
    items.forEach(function (item) {
      if (!map.has(item.group)) {
        map.set(item.group, []);
        order.push(item.group);
      }
      map.get(item.group).push(item);
    });
    return order.map(function (title) {
      return { title: title, items: map.get(title) };
    });
  }

  function baseState() {
    return {
      view: "home",
      listener: false,
      pro: false,
      redact: true,
      quiet: false,
      quietFrom: 22,
      quietTo: 7,
      headphones: false,
      shake: false,
      rate: 1,
      volume: 1,
      filters: [],
      template: "",
      appTemplates: {},
      query: "",
      selected: ["wa", "tg", "gm"],
      speakingNow: false,
      offer: "buy",
      loading: false,
      sheet: null,
      templateTarget: null,
      draft: "",
      brand: "samsung",
      toast: "",
      back: "home"
    };
  }

  function injectCss() {
    if (document.getElementById("teloleo-sys")) return;
    var style = document.createElement("style");
    style.id = "teloleo-sys";
    style.textContent = [
      ".screen .viewport.sys{background:#EEF0F3;color:#1C1D1F;padding:12px 18px 32px;min-height:100%;}",
      ".screen:has(.viewport.sys){background:#EEF0F3;color:#1C1D1F;}",
      ".screen .viewport.sys h1{font-family:'Segoe UI',sans-serif;font-size:30px;line-height:1.15;font-weight:650;letter-spacing:0;margin:8px 0 10px;color:#1C1D1F;}",
      ".screen .viewport.sys .sysflag{margin:8px 0;font-size:12px;font-weight:700;letter-spacing:.14em;text-transform:uppercase;color:#5F6368;}",
      ".screen .viewport.sys .syslead{font-size:15px;line-height:1.45;color:#3C4043;margin-bottom:16px;}",
      ".screen .viewport.sys .sysrow{display:flex;align-items:center;gap:12px;background:#fff;border-radius:16px;padding:14px;margin-bottom:18px;cursor:pointer;}",
      ".screen .viewport.sys .sysrow span{flex:1;min-width:0;}",
      ".screen .viewport.sys .sysrow strong,.screen .viewport.sys .sysrow small{display:block;}",
      ".screen .viewport.sys .sysrow small{color:#5F6368;font-size:13px;margin-top:2px;}",
      ".screen .viewport.sys .btn{background:#0B57D0;color:#fff;border:0;border-radius:999px;min-height:48px;width:100%;font-weight:650;}",
      ".screen .viewport.sys .switch{background:#DADCE0;}",
      ".screen .viewport.sys .switch.on{background:#0B57D0;}"
    ].join("");
    document.head.appendChild(style);
  }

  function boot(config) {
    injectCss();
    var state = baseState();
    var presetId = "";
    var toastTimer = 0;
    var grouped = config.groups;

    function setHash(id) {
      try { history.replaceState(null, "", id ? "#" + id : location.pathname + location.search); }
      catch (err) { /* file:// puede rechazar el hash */ }
    }

    function hh(hour) {
      return String(hour).padStart(2, "0") + ":00";
    }

    function fmt(n) {
      return (Math.round(Number(n) * 10) / 10).toFixed(1).replace(".", ",");
    }

    function sw(on) {
      return '<span class="switch' + (on ? " on" : "") + '" aria-hidden="true"><i></i></span>';
    }

    function speakingIds() {
      return state.pro ? state.selected.slice() : state.selected.slice(0, 3);
    }

    function appById(id) {
      return APPS.find(function (app) { return app.id === id; });
    }

    function decorate(app) {
      var order = state.selected.indexOf(app.id) + 1;
      var speaks = speakingIds().indexOf(app.id) !== -1;
      var selected = order > 0;
      return {
        id: app.id,
        name: app.name,
        pkg: app.pkg,
        color: app.color,
        selected: selected,
        order: order,
        speaks: speaks,
        extra: selected && !speaks,
        template: state.appTemplates[app.id] || ""
      };
    }

    function matches(app) {
      var q = state.query.trim().toLowerCase();
      if (!q) return true;
      return app.name.toLowerCase().indexOf(q) !== -1 || app.pkg.toLowerCase().indexOf(q) !== -1;
    }

    function selectedVisible() {
      return state.selected.map(appById).filter(Boolean).filter(matches).map(decorate);
    }

    function othersVisible() {
      return APPS.filter(function (app) {
        return state.selected.indexOf(app.id) === -1 && matches(app);
      }).map(decorate);
    }

    function speakingNames() {
      return speakingIds().map(function (id) {
        var app = appById(id);
        return app ? app.name : "";
      }).filter(Boolean);
    }

    function joinNames(names) {
      if (!names.length) return "Ninguna app marcada";
      if (names.length === 1) return names[0];
      return names.slice(0, -1).join(", ") + " y " + names[names.length - 1];
    }

    function countLine() {
      var n = state.selected.length;
      return state.pro
        ? "Marcadas: " + n + ". Pro lee todas."
        : "Marcadas: " + n + ". Sin Pro se leen 3 apps.";
    }

    function quotaNote() {
      return state.pro
        ? "Pro lee todas las que marques."
        : "La cuarta no se añade.";
    }

    function phrase(tpl) {
      var raw = (tpl == null ? "" : String(tpl)).trim() || "{app}. {remitente}. {texto}";
      var text = state.redact ? "El código está oculto" : "Tu código es 482913";
      var out = raw.replace(/\{(app|remitente|titulo|texto)\}/gi, function (_, key) {
        var k = key.toLowerCase();
        if (k === "app") return "WhatsApp";
        if (k === "texto") return text;
        return "Ana";
      });
      out = out.replace(/\s+/g, " ").trim();
      if (out && !/[.?!…]$/.test(out)) out += ".";
      return out;
    }

    function livePhrase() {
      if (state.pro) {
        var specific = (state.appTemplates.wa || "").trim();
        if (specific) return phrase(specific);
        if (state.template.trim()) return phrase(state.template);
      }
      return phrase("");
    }

    function rules() {
      if (!state.pro) return [];
      var out = [];
      if (state.quiet) out.push("Silencio " + hh(state.quietFrom) + "–" + hh(state.quietTo));
      if (state.headphones) out.push("Solo auriculares");
      if (state.shake) out.push("Agitar calla 2 min");
      if (state.filters.length) out.push("Con filtro");
      if (state.template.trim()) out.push("Frase propia");
      return out;
    }

    function hours(value, act) {
      var label = act === "quiet-from" ? COPY.from : COPY.until;
      var options = "";
      for (var h = 0; h < 24; h++) {
        options += '<option value="' + h + '"' + (h === value ? " selected" : "") + ">" + hh(h) + "</option>";
      }
      return '<label class="time"><span>' + label + "</span><select data-act=\"" + act + "\" aria-label=\"" + label + "\">" + options + "</select></label>";
    }

    function brand() {
      var item = BRANDS[state.brand] || BRANDS.samsung;
      return { id: state.brand, name: item[0], text: item[1] };
    }

    function offerModel() {
      var active = !!state.pro;
      var table = {
        buy: { label: "Comprar Pro", disabled: false, notice: "", pending: false, priced: false },
        price: { label: "Comprar Pro · US$ 4,99", disabled: false, notice: "", pending: false, priced: true },
        pending: { label: "Comprar Pro · US$ 4,99", disabled: false, notice: "", pending: true, priced: true },
        unavailable: { label: "Comprar Pro", disabled: true, notice: "Play Store no está disponible en este teléfono.", pending: false, priced: false },
        missing: { label: "Comprar Pro", disabled: true, notice: "El precio aparece cuando el producto está activo en Play Console.", pending: false, priced: false },
        failed: { label: "Comprar Pro · US$ 4,99", disabled: false, notice: "No se pudo completar la compra.", pending: false, priced: true },
        active: { label: "", disabled: true, notice: "", pending: false, priced: false }
      };
      var row = table[state.offer] || table.buy;
      return {
        active: active,
        label: row.label,
        disabled: row.disabled,
        notice: row.notice,
        pending: row.pending && !active,
        priced: row.priced && !active,
        showBuy: !active
      };
    }

    function systemPage() {
      var on = state.listener;
      return '<div class="viewport sys">' +
        '<p class="sysflag">Pantalla del sistema</p>' +
        "<h1>Acceso a las notificaciones</h1>" +
        '<p class="syslead">Android pide este permiso para cualquier lector de avisos. TeLoLeo solo lee las apps que marques. El texto no sale del teléfono.</p>' +
        '<div class="sysrow" role="button" tabindex="0" data-act="system-toggle">' +
          "<span><strong>TeLoLeo</strong><small>" + (on ? "Permitido. Puedes quitarlo cuando quieras." : "No permitido") + "</small></span>" +
          sw(on) +
        "</div>" +
        '<button type="button" class="btn" data-act="system-done">Volver a TeLoLeo</button>' +
      "</div>";
    }

    function sheetBody() {
      if (state.sheet === "disclosure") {
        return "<h2>" + COPY.disclosureTitle + "</h2>" +
          '<ol class="steps">' + DISCLOSURE.map(function (text, i) {
            return "<li><b>" + (i + 1) + "</b><span>" + text + "</span></li>";
          }).join("") + "</ol>" +
          '<p class="note">' + COPY.disclosureNext + "</p>" +
          '<div class="actions">' +
            '<button type="button" class="btn" data-act="confirm-disclosure">' + COPY.disclosureOk + "</button>" +
            '<button type="button" class="btn ghost" data-act="close-sheet">' + COPY.notNow + "</button>" +
          "</div>";
      }
      if (state.sheet === "limit") {
        return "<h2>" + COPY.limitTitle + "</h2>" +
          "<p>" + COPY.limitBody + "</p>" +
          '<p class="note">' + COPY.limitOrder + "</p>" +
          '<div class="actions">' +
            '<button type="button" class="btn" data-act="see-pro">' + COPY.seePro + "</button>" +
            '<button type="button" class="btn ghost" data-act="close-sheet">' + COPY.notNow + "</button>" +
          "</div>";
      }
      if (state.sheet === "battery") {
        var b = brand();
        return "<h2>" + COPY.battery + "</h2>" +
          '<p class="kicker">' + esc(b.name) + "</p>" +
          "<p>" + esc(b.text) + "</p>" +
          '<p class="note">La app elige este texto según la marca del teléfono.</p>' +
          '<div class="actions"><button type="button" class="btn" data-act="close-sheet">Listo</button></div>';
      }
      var title = "Plantilla";
      if (state.templateTarget) {
        var found = appById(state.templateTarget);
        title = found ? found.name : "Plantilla";
      }
      return "<h2>" + esc(title) + "</h2>" +
        "<p>" + COPY.templateHelp + " " + COPY.templateAlias + "</p>" +
        '<textarea id="draft">' + esc(state.draft) + "</textarea>" +
        '<p class="kicker">Así sonaría</p>' +
        '<p class="preview" id="preview">' + esc(phrase(state.draft)) + "</p>" +
        '<div class="actions">' +
          '<button type="button" class="btn" data-act="save-template">' + COPY.save + "</button>" +
          '<button type="button" class="btn ghost" data-act="close-sheet">' + COPY.cancel + "</button>" +
        "</div>";
    }

    function overlayHtml() {
      if (!state.sheet) return "";
      return '<div class="overlay sheet-' + state.sheet + '">' +
        '<div class="shade" data-act="backdrop"></div>' +
        '<div class="panel" role="dialog" aria-modal="true"><div class="handle"></div>' + sheetBody() + "</div>" +
      "</div>";
    }

    function toastHtml() {
      if (!state.toast) return "";
      return '<div class="toast" role="status">' + esc(state.toast) + "</div>";
    }

    function armToast() {
      clearTimeout(toastTimer);
      var msg = state.toast;
      toastTimer = setTimeout(function () {
        if (state.toast === msg) {
          state.toast = "";
          render();
        }
      }, 2600);
    }

    function findPreset(id) {
      var found = null;
      grouped.forEach(function (group) {
        group.items.forEach(function (item) {
          if (item.id === id) found = item;
        });
      });
      return found;
    }

    var api = {
      esc: esc,
      sw: sw,
      copy: COPY,
      privacy: PRIVACY,
      features: FEATURES,
      selectedVisible: selectedVisible,
      othersVisible: othersVisible,
      speakingIds: speakingIds,
      speakingNames: speakingNames,
      joinNames: joinNames,
      countLine: countLine,
      quotaNote: quotaNote,
      phrase: phrase,
      livePhrase: livePhrase,
      hh: hh,
      hours: hours,
      fmt: fmt,
      rules: rules,
      brand: brand,
      offerModel: offerModel,
      systemPage: systemPage,
      get state() { return state; }
    };

    function render() {
      var active = document.activeElement;
      var focusId = active && active.closest && active.closest("#screen") ? active.id : "";
      var pos = focusId && typeof active.selectionStart === "number" ? active.selectionStart : null;
      document.getElementById("screen").innerHTML =
        '<div class="statusbar"><span>9:41</span><span class="sigs">5G</span></div>' +
        config.renderPage(api) +
        overlayHtml() +
        toastHtml();
      document.querySelectorAll("[data-preset]").forEach(function (btn) {
        btn.classList.toggle("on", btn.dataset.preset === presetId);
      });
      var brands = document.getElementById("brands");
      if (brands) {
        brands.innerHTML = Object.keys(BRANDS).map(function (id) {
          return '<button type="button" data-brand="' + id + '"' + (state.brand === id ? ' class="on"' : "") + ">" + esc(BRANDS[id][0]) + "</button>";
        }).join("");
      }
      var now = document.getElementById("now");
      if (now) {
        var current = findPreset(presetId);
        now.textContent = current ? "Vista: " + current.label : "Vista modificada en la maqueta";
      }
      if (focusId) {
        var next = document.getElementById(focusId);
        if (next) {
          next.focus();
          if (pos != null && next.setSelectionRange) {
            try { next.setSelectionRange(pos, pos); } catch (err) { /* range */ }
          }
        }
      }
    }

    function apply(id) {
      var item = findPreset(id);
      if (!item) return;
      presetId = id;
      state = Object.assign(baseState(), structuredClone(item.patch));
      setHash(id);
      render();
    }

    function openSystem() {
      if (state.view !== "sistema") state.back = state.view;
      state.sheet = null;
      state.view = "sistema";
    }

    function gopro() {
      if (state.view !== "pro") state.back = state.view;
      state.sheet = null;
      state.view = "pro";
    }

    function onAct(act, el) {
      var id = el.dataset.id || "";
      if (act === "backdrop" && state.sheet === "disclosure") return;
      presetId = "";
      setHash("");
      switch (act) {
        case "toggle":
          if (state.selected.indexOf(id) !== -1) {
            state.selected = state.selected.filter(function (item) { return item !== id; });
          } else if (!state.pro && state.selected.length >= 3) {
            state.sheet = "limit";
          } else if (id) {
            state.selected = state.selected.concat(id);
          }
          break;
        case "template":
          if (!state.pro) { gopro(); break; }
          state.templateTarget = id || null;
          state.draft = id ? (state.appTemplates[id] || "") : state.template;
          state.sheet = "template";
          break;
        case "save-template":
          var value = state.draft.trim();
          if (state.templateTarget) {
            if (value) state.appTemplates[state.templateTarget] = value;
            else delete state.appTemplates[state.templateTarget];
          } else {
            state.template = value;
          }
          state.sheet = null;
          state.toast = "Plantilla guardada en el teléfono.";
          armToast();
          break;
        case "redact":
          state.redact = !state.redact;
          break;
        case "listener":
          if (state.listener) openSystem();
          else state.sheet = "disclosure";
          break;
        case "confirm-disclosure":
          openSystem();
          break;
        case "system-toggle":
          state.listener = !state.listener;
          break;
        case "system-done":
        case "back":
          state.view = state.back && state.back !== "sistema" ? state.back : "home";
          state.sheet = null;
          break;
        case "silence":
          state.speakingNow = false;
          state.toast = "En silencio durante 2 minutos.";
          armToast();
          break;
        case "gopro":
        case "see-pro":
          gopro();
          break;
        case "privacy":
          if (state.view !== "privacy") state.back = state.view;
          state.sheet = null;
          state.view = "privacy";
          break;
        case "goto":
          state.view = id || "home";
          state.sheet = null;
          break;
        case "battery":
          state.sheet = "battery";
          break;
        case "close-sheet":
        case "backdrop":
          state.sheet = null;
          break;
        case "quiet":
          if (!state.pro) gopro();
          else state.quiet = !state.quiet;
          break;
        case "headphones":
          if (!state.pro) gopro();
          else state.headphones = !state.headphones;
          break;
        case "shake":
          if (!state.pro) gopro();
          else state.shake = !state.shake;
          break;
        case "addfilter":
          if (!state.pro) { gopro(); break; }
          var raw = (document.getElementById("filter") || {}).value || "";
          raw.split(",").map(function (part) { return part.trim(); }).filter(Boolean).forEach(function (word) {
            var exists = state.filters.some(function (item) { return item.toLowerCase() === word.toLowerCase(); });
            if (!exists) state.filters.push(word);
          });
          break;
        case "unfilter":
          state.filters.splice(Number(el.dataset.i), 1);
          break;
        case "clear-search":
          state.query = "";
          break;
        case "buy":
          state.toast = "Esto abre la hoja de pago de Play.";
          armToast();
          break;
        case "restore":
          state.toast = "Busca la compra en esta cuenta de Play.";
          armToast();
          break;
        default:
          break;
      }
      render();
    }

    document.body.addEventListener("click", function (event) {
      var preset = event.target.closest("[data-preset]");
      if (preset) { apply(preset.dataset.preset); return; }
      var brandBtn = event.target.closest("[data-brand]");
      if (brandBtn) {
        state.brand = brandBtn.dataset.brand;
        if (state.view === "pro" || state.view === "privacy" || state.view === "sistema") {
          state.view = config.batteryView || "home";
        }
        state.sheet = "battery";
        presetId = "";
        setHash("");
        render();
        return;
      }
      var el = event.target.closest("[data-act]");
      if (!el || !el.closest("#screen")) return;
      var tag = el.tagName;
      if (tag === "SELECT" || tag === "OPTION" || tag === "TEXTAREA" || tag === "INPUT") return;
      onAct(el.dataset.act, el);
    });

    document.body.addEventListener("keydown", function (event) {
      if (event.key !== "Enter" && event.key !== " ") return;
      var el = event.target.closest && event.target.closest("[data-act]");
      if (!el || !el.closest("#screen")) return;
      var tag = el.tagName;
      if (tag === "BUTTON" || tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT" || tag === "OPTION") return;
      event.preventDefault();
      onAct(el.dataset.act, el);
    });

    document.body.addEventListener("input", function (event) {
      var el = event.target;
      if (!el.closest || !el.closest("#screen")) return;
      if (el.id === "q") {
        state.query = el.value;
        presetId = "";
        setHash("");
        render();
        return;
      }
      if (el.id === "draft") {
        state.draft = el.value;
        var preview = document.getElementById("preview");
        if (preview) preview.textContent = phrase(el.value);
        return;
      }
      if (el.id === "rate") {
        state.rate = parseFloat(el.value);
        var rate = document.getElementById("rate-val");
        if (rate) rate.textContent = fmt(state.rate);
        return;
      }
      if (el.id === "vol") {
        state.volume = parseFloat(el.value);
        var vol = document.getElementById("vol-val");
        if (vol) vol.textContent = fmt(state.volume);
      }
    });

    document.body.addEventListener("change", function (event) {
      var el = event.target;
      if (!el.closest || !el.closest("#screen")) return;
      if (el.dataset.act !== "quiet-from" && el.dataset.act !== "quiet-to") return;
      if (el.dataset.act === "quiet-from") state.quietFrom = Number(el.value);
      else state.quietTo = Number(el.value);
      presetId = "";
      setHash("");
      render();
    });

    var rail = document.getElementById("rail");
    if (rail) {
      rail.innerHTML = grouped.map(function (group) {
        return "<h3>" + esc(group.title) + "</h3>" + group.items.map(function (item) {
          return '<button type="button" data-preset="' + item.id + '">' + esc(item.label) + "</button>";
        }).join("");
      }).join("");
    }

    var start = (location.hash || "").replace("#", "");
    apply(findPreset(start) ? start : "home-off");
  }

  window.TeLoLeo = { boot: boot, groups: groups };
})();
