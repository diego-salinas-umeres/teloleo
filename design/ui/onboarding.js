/* Datos y piezas compartidas de las maquetas de onboarding. No es la app. */
(function () {
  "use strict";

  var APPS = [
    { id: "wa", name: "WhatsApp", letter: "W", color: "#128C7E" },
    { id: "tg", name: "Telegram", letter: "T", color: "#2AABEE" },
    { id: "gm", name: "Gmail", letter: "G", color: "#C5221F" },
    { id: "sms", name: "Mensajes", letter: "M", color: "#1A73E8" },
    { id: "ig", name: "Instagram", letter: "I", color: "#C13584" },
    { id: "signal", name: "Signal", letter: "S", color: "#3A76F0" },
    { id: "cal", name: "Calendario", letter: "C", color: "#4285F4" },
    { id: "slack", name: "Slack", letter: "Sl", color: "#4A154B" }
  ];

  var PHRASES = {
    wa: { who: "Ana", text: "Ya salí.", spoken: "WhatsApp. Ana. Ya salí." },
    tg: { who: "Luis", text: "¿Llegas a las 7?", spoken: "Telegram. Luis. ¿Llegas a las 7?" },
    gm: { who: "Banco", text: "Tu código es 482913.", spoken: "Gmail. Banco. Tu código es oculto.", hidden: true },
    sms: { who: "Ana", text: "Estoy abajo.", spoken: "Mensajes. Ana. Estoy abajo." },
    ig: { who: "Ana", text: "Te mencionó en una historia.", spoken: "Instagram. Ana. Te mencionó en una historia." },
    signal: { who: "Ana", text: "Ya llegué.", spoken: "Signal. Ana. Ya llegué." },
    cal: { who: "Hoy", text: "Reunión en 10 minutos.", spoken: "Calendario. Reunión en 10 minutos." },
    slack: { who: "Equipo", text: "¿Lo ves?", spoken: "Slack. Equipo. ¿Lo ves?" }
  };

  var FEATURES = [
    "Apps ilimitadas",
    "Horario de silencio",
    "Solo auriculares o Bluetooth",
    "Plantilla de la frase",
    "Filtro de palabras",
    "Agitar para callar",
    "Velocidad y volumen propios"
  ];

  function esc(value) {
    return String(value == null ? "" : value).replace(/[&<>"']/g, function (ch) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[ch];
    });
  }

  function byId(id) {
    for (var i = 0; i < APPS.length; i++) {
      if (APPS[i].id === id) return APPS[i];
    }
    return APPS[0];
  }

  function phrase(id) {
    return PHRASES[id] || PHRASES.wa;
  }

  function listNames(ids) {
    var names = (ids || []).map(function (id) { return byId(id).name; });
    if (names.length <= 1) return names[0] || "";
    if (names.length === 2) return names[0] + " y " + names[1];
    return names.slice(0, -1).join(", ") + " y " + names[names.length - 1];
  }

  function sampleAt(index) {
    var ids = ["wa", "gm", "tg"];
    var id = ids[((index % ids.length) + ids.length) % ids.length];
    var line = phrase(id);
    return { id: id, app: byId(id), line: line, kicker: byId(id).name + " · " + line.who };
  }

  function row(app, on) {
    return '<button type="button" class="app' + (on ? " on" : "") + '" data-act="app" data-id="' + app.id + '" aria-pressed="' + (on ? "true" : "false") + '">' +
      '<i class="av" style="background:' + app.color + '">' + esc(app.letter) + "</i>" +
      '<span class="meta"><strong>' + esc(app.name) + "</strong></span>" +
      '<span class="tick" aria-hidden="true"></span></button>';
  }

  function rows(selected, query) {
    var q = String(query || "").trim().toLowerCase();
    var chosenIds = selected || [];
    var list = APPS.filter(function (app) {
      return !q || app.name.toLowerCase().indexOf(q) !== -1;
    });
    if (!list.length) return '<p class="empty">Ninguna app coincide.</p>';
    var chosen = list.filter(function (app) { return chosenIds.indexOf(app.id) !== -1; });
    var rest = list.filter(function (app) { return chosenIds.indexOf(app.id) === -1; });
    if (!q && chosen.length) {
      var html = '<p class="group">Se leen</p>' + chosen.map(function (app) { return row(app, true); }).join("");
      if (rest.length) html += '<p class="group">Otras apps</p>' + rest.map(function (app) { return row(app, false); }).join("");
      return html;
    }
    return list.map(function (app) { return row(app, chosenIds.indexOf(app.id) !== -1); }).join("");
  }

  function system(opts) {
    opts = opts || {};
    return '<div class="os">' +
      '<div class="os-status"><span>9:41</span><span>5G</span></div>' +
      '<div class="os-top"><button type="button" data-act="system-back">Atrás</button><span>Ajustes</span></div>' +
      "<h2>Acceso a las notificaciones</h2>" +
      '<p class="os-lead">Las apps de esta lista pueden leer todas las notificaciones.</p>' +
      '<button type="button" class="os-row" data-act="switch"><i class="av">T</i><span class="who"><b>TeLoLeo</b><small>app.teloleo</small></span><span class="knob" aria-hidden="true"></span></button>' +
      (opts.hint ? '<p class="os-hint">' + esc(opts.hint) + "</p>" : "") +
      (opts.dialog ?
        '<div class="scrim"><div class="dialog" role="dialog" aria-modal="true" aria-labelledby="os-title">' +
        '<h3 id="os-title">¿Permitir que TeLoLeo lea las notificaciones?</h3>' +
        "<p>Podrá leer todas las notificaciones, incluidos contactos, mensajes y fotos, y actuar sobre ellas.</p>" +
        '<div class="dialog-actions"><button type="button" data-act="deny">No permitir</button><button type="button" data-act="allow">Permitir</button></div>' +
        "</div></div>" : "") +
      "</div>";
  }

  function limit(name, pro) {
    var feats = "<ul>" + FEATURES.map(function (item) { return "<li>" + esc(item) + "</li>"; }).join("") + "</ul>";
    return '<div class="scrim"><div class="sheet" role="dialog" aria-modal="true" aria-labelledby="limit-title">' +
      '<h3 id="limit-title">3 apps en la versión gratis</h3>' +
      "<p><strong>" + esc(name) + "</strong> no se añade. Pro lee todas las que elijas, con un solo pago. No es una suscripción.</p>" +
      (pro ? feats : "") +
      '<button type="button" class="btn" data-act="limit-close">Ahora no</button>' +
      (pro ? "" : '<button type="button" class="btn ghost" data-act="limit-pro">Qué incluye Pro</button>') +
      "</div></div>";
  }

  window.Onboarding = {
    APPS: APPS,
    esc: esc,
    byId: byId,
    phrase: phrase,
    listNames: listNames,
    sampleAt: sampleAt,
    rows: rows,
    system: system,
    limit: limit
  };
})();
