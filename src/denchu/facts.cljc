(ns denchu.facts
  "電柱広告の掲出に関する**管轄別の法令基盤カタログ**。

  `cloud-itonami-isic-7310` の `advertising.facts` と同じ規律で書く:
  ここに無い管轄は spec-basis が **無い**（advisor がでっち上げてよい、では
  ない）。カバレッジは `coverage` が正直に返す。

  日本の電柱広告に固有の構造 — これを取り違えると設計が嘘になる:

  1. 屋外広告物法に基づき、**各自治体の屋外広告物条例**が許可を出す。
     国が直接許可するのではない（都道府県・政令市等が条例を持つ）。
  2. 多くの条例は電柱・街路灯柱への**貼り紙・貼り札・広告旗を禁止物件**
     として列挙する。一方、電力会社・通信会社の柱に所定の手続きで付ける
     巻付/袖看板は許可対象として別に扱われる。
     **「電柱への広告は条例で禁止」と要約しない** — 禁止されているのは
     無断の貼付物であり、許可を得た掲出物ではない。
  3. 道路上空に突き出す場合は道路法の**道路占用許可**が別途要る。
  4. 実務上、これらの申請は代理店が代行することが多い（テルウェル東日本は
     道路占用申請・NTT電柱使用許可申請を自社および販売会社が行うと明記）。
     代行される事実は、要件が消えることを意味しない。"
  (:require [clojure.string :as str]))

(def catalog
  "iso3 → 要件。`:required-evidence` は governor が掲出提案を通す前に
  実在を要求する証跡。`:legal-basis` / `:owner-authority` / `:provenance`
  は出典。"
  {"JPN"
   {:name "Japan"
    :owner-authority "各都道府県・政令指定都市等（屋外広告物条例） / 道路管理者（道路占用許可）"
    :legal-basis "屋外広告物法、および同法に基づく各自治体の屋外広告物条例／道路法（道路占用許可）"
    :national-spec "電柱への広告掲出は、設備所有者（電力会社・通信会社）の使用許諾に加え、掲出地の屋外広告物条例に基づく許可を要する。道路上空に突出する形態は道路占用許可の対象になりうる。"
    :prohibited-note "多数の条例が電柱・街路灯柱への貼り紙・貼り札・広告旗を禁止物件として列挙する。これは無断の貼付物に対する規制であり、所有者の許諾と条例許可を経た巻付／袖看板とは別扱い。"
    :provenance ["https://www.rilg.or.jp/htdocs/img/reiki/057_outdoor_advertising.htm"
                 "https://www.city.osaka.lg.jp/kensetsu/page/0000372127.html"
                 "https://www.toaa.or.jp/jyou/"]
    :required-evidence ["設備所有者の使用許諾記録 (pole-owner-consent-record)"
                        "屋外広告物許可申請記録 (outdoor-ad-permit-record)"
                        "道路占用許可記録（突出形態の場合） (road-occupancy-permit-record)"
                        "代理店申込記録 (agency-order-record)"
                        "掲出物仕様・製作記録 (creative-spec-record)"]
    :permit-authority-is-municipal? true
    :as-of "2026-08-04"}})

(defn requirements [iso3] (get catalog iso3))

(defn covered? [iso3] (contains? catalog iso3))

(defn coverage
  "正直なカバレッジ申告。1 管轄しか無いことを隠さない。"
  []
  {:jurisdictions (vec (sort (keys catalog)))
   :count (count catalog)
   :note (str "収録 " (count catalog) " 管轄。未収録の管轄は spec-basis 無し — "
              "advisor は要件を創作してはならず、governor は掲出提案を保留する。")})

(defn missing-evidence
  "掲出提案が持つべき証跡のうち、まだ無いもの。管轄が未収録なら
  `:no-spec-basis` を返す（空 vector ではない — 空は『全部揃っている』と
  読めてしまう）。"
  [iso3 provided-evidence-keys]
  (if-let [req (requirements iso3)]
    (let [have (set (map str provided-evidence-keys))]
      (vec (remove (fn [e]
                     (some (fn [h] (str/includes? e h)) have))
                   (:required-evidence req))))
    :no-spec-basis))

(defn road-occupancy-relevant?
  "その掲出面が道路占用許可の検討対象になりうるか。`denchu.slot` の
  `:slot/overhangs-road?` と対で使う。判断は道路管理者が行う。"
  [iso3 slot-kind]
  (and (covered? iso3) (= slot-kind :projecting)))
