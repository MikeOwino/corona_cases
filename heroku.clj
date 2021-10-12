#!/usr/bin/env bb

(load-file "src/corona/envdef.clj")
(load-file "src/corona/pom_version_get.clj")

(ns heroku
  "See https://github.com/clojure/tools.cli"
  (:require
   [clojure.string :as cstr]
   [corona.envdef :as env]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as jio]
   [corona.pom-version-get :as pom]
   )
  (:import
   java.lang.ProcessBuilder$Redirect
   ))

(defn get-heroku-envs [kws]
  ((comp
    set
    vals
    (partial into {})
    (partial map (fn [kw] (get-in env/environment [kw :cli]))))
    kws))

(def heroku-envs (get-heroku-envs (keys env/environment)))
#_(println "heroku-envs" heroku-envs)

(def heroku-env-prod ((comp first
                            get-heroku-envs
                            vector
                            keyword)
                      env/corona-cases))
#_(println "heroku-env-prod" heroku-env-prod)

(def heroku-apps
  ((comp
    set
    (partial map (fn [henv] (str henv "-bot"))))
   heroku-envs))
#_(println "heroku-apps" heroku-apps)

#_(System/exit 1)

(defn in?
  "true if `sequence` contains `elem`. See (contains? (set sequence) elem)"
  [sequence elem]
  (boolean (some (fn [e] (= elem e)) sequence)))

(def cli-options
  ;; An option with a required argument
  [["-e" "--heroku-env HENV" "Required Heroku environment to run command against"
    :validate [(fn [henv] (in? heroku-envs henv))
               (str "Must be an element of " heroku-envs)]]
   ["-f" "--force" "Force deployment"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Usage: program-name action [options]"
        ""
        "Options:"
        options-summary]
       (cstr/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (cstr/join \newline errors)))

;; actions
(def restart "restart")
(def deploy "deploy")
(def deleteWebhook "deleteWebhook")
(def setWebhook "setWebhook")
(def users "users")
(def promote "promote")
(def getMockData "getMockData")
(def getLogs "getLogs")

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}

      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}

      ;; custom validation on arguments
      (= 1 (count arguments))
      (cond
        (#{restart deploy deleteWebhook setWebhook users promote getMockData
           getLogs}
         (first arguments))
        {:action (first arguments) :options options}

        :else
        {:exit-message (format "Unknown action: %s\n\n%s"
                               (first arguments) (usage summary))})

      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn trim-last-newline [s]
  (if (and (not-empty s) (.endsWith s "\n"))
    (.substring s 0 (dec (count s)))
    s))

(defn shell-command
  "Executes shell command. Exits script when the shell-command has a non-zero
  exit code, propagating it. Accepts the following options:
  `:input`: instead of reading from stdin, read from this string.
  `:to-string?`: instead of writing to stdoud, write to a string and
  return it."
  ([args] (shell-command args nil))
  ([args {:keys [:input :to-string?]}]
   (let [args (mapv str args)
         pb (cond-> (-> (ProcessBuilder. ^java.util.List args)
                        (.redirectError ProcessBuilder$Redirect/INHERIT))
              (not to-string?)
              (.redirectOutput ProcessBuilder$Redirect/INHERIT)
              #_(.redirectOutput ProcessBuilder$Redirect/PIPE)
              #_(.redirectOutput (ProcessBuilder$Redirect/appendTo temp))

              (not input) (.redirectInput ProcessBuilder$Redirect/INHERIT))
         proc (.start pb)]
     (when input
       (with-open [w (jio/writer (.getOutputStream proc))]
         (binding [*out* w]
           (print input)
           (flush))))
     (let [string-out
           (when to-string? ;; i.e when output not redirected
             #_(println "Not redirected; reading from proc to-string ...")
             (let [sw (java.io.StringWriter.)]
               (with-open [w (jio/reader
                              (.getInputStream proc)
                              #_(jio/input-stream (File. (.toString temp)))
                              )]
                 (jio/copy w sw))
               (str sw)))
           exit-code (.waitFor proc)]
       (when-not (zero? exit-code)
         (System/exit exit-code))
       (printf "%s\n" string-out)
       string-out))))

(defn sh [& raw-args]
    (let [args (remove empty? raw-args)]
      (println "#" (cstr/join " " args))
      (->> (shell-command args
                          (conj
                           #_{:to-string? false}
                           {:to-string? true}))
           (trim-last-newline))))

(defn sh-heroku
  [app & cmds]
  {:pre [(in? heroku-apps app)]}
  (apply sh (into ["heroku"] (conj (vec cmds) "--app" app))))

(defn get-commit! []
  (sh "git" "rev-parse" "--short" "master"))

(defn set-config! [app commit clojure-cli-version]
    {:pre [(in? heroku-apps app)]}
  (sh-heroku app "config:set" (str "COMMIT=" commit) clojure-cli-version))

(defn stop! [app]
  {:pre [(in? heroku-apps app)]}
  (sh-heroku app "ps:scale" "web=0"))

(defn start! [app]
  {:pre [(in? heroku-apps app)]}
  (sh-heroku app "ps:scale" "web=1"))

(defn restart!
  "There's a special heroku command for it"
  [app]
  {:pre [(in? heroku-apps app)]}
  (sh-heroku app "ps:restart"))

(defn open-papertrail [app]
  {:pre [(in? heroku-apps app)]}
  (sh-heroku app "addons:open" "papertrail"))

(defn publish-source!
  "Publish the source code only when deploying to production"
  [commit rest-args]
  ;; seems like `git push --tags` pushes only tags w/o the code
  (sh "git" "tag" "--annotate" "--message" "''"
      (str pom/pom-version "-" commit))
  (doseq [remote ["origin" "gitlab"]]
    ;; See also `git push --tags $pushFlags $remote`
    (sh "git" "push" rest-args "--follow-tags" "--verbose" remote)))

(defn deploy! [prm-app options]
  {:pre [(in? heroku-apps prm-app)]}
  (let [commit (get-commit!)
        clojure-cli-version (let [props (java.util.Properties.)
                                  key "CLOJURE_CLI_VERSION"]
                              (.load props (jio/reader ".heroku-local.env"))
                              (str key "=" (get props key)))
        heroku-env (:heroku-env options)
        app (str heroku-env "-bot")
        remote (str "heroku-" app)
        rest-args (if (:force options) "--force" "")]
    (open-papertrail app)
    (stop! app)
    (set-config! app commit clojure-cli-version)
    (sh "git" "push" rest-args remote "master")
    (start! app)
    (when (= heroku-env heroku-env-prod)
      (publish-source! commit rest-args))))

(defn promote! [{:keys [src-app dst-app]} options]
  {:pre [(and (not= src-app dst-app)
              (in? heroku-apps src-app)
              (in? heroku-apps dst-app))]}
  (let [commit (get-commit!)
        clojure-cli-version (let [props (java.util.Properties.)
                                  key "CLOJURE_CLI_VERSION"]
                              (.load props (jio/reader ".heroku-local.env"))
                              (str key "=" (get props key)))
        rest-args (if (:force options) "--force" "")]
    (open-papertrail dst-app)
    (stop! dst-app)
    (set-config! dst-app commit clojure-cli-version)
    (sh-heroku src-app "pipelines:promote")
    (start! dst-app)
    (publish-source! commit rest-args)))

;; Examples:
;; ./heroku.clj deploy --app hokuspokus-bot
(let [{:keys [action options exit-message ok?]}
      (validate-args *command-line-args*)]
  (if exit-message
    (exit (if ok? 0 1) exit-message)
    (let [heroku-env (:heroku-env options)
          heroku-app (str heroku-env "-bot")
          telegram-token (get-in env/environment
                                 [(keyword heroku-env) :telegram-token])]
      (condp = action
        restart (restart! heroku-app)
        deploy  (deploy! heroku-app options)
        promote (promote! {:src-app "hokuspokus-bot" :dst-app "corona-cases-bot"} options)

        #_(str "
         # Search in logs:
         rg --search-zip SEARCH_TERM ./log/
         gzip -cd ./log/* | grep SEARCH_TERM
         gzip --to-stdout --decompress ./log/* | grep SEARCH_TERM

         heroku logs --num 1500 --app $APP # last 1500 lines
         heroku logs --tail --app $APP
         ")
        ;; TODO plot log entries on a timescale:
        ;; parse the logfile, compute timestamp diffs
        getLogs
        (let [log-dir (str "log/" heroku-env)
              pt-token (sh-heroku heroku-app "config:get" "PAPERTRAIL_API_TOKEN")
              pt-header (format "X-Papertrail-Token: %s" pt-token)]

          (sh "mkdir" "-p" log-dir)

          (doseq [hour-ago '(0 1)]
            ;; It takes approximately 6-7 hours for logs to be available in the archive.
            ;; https://help.papertrailapp.com/kb/how-it-works/permanent-log-archives

            (let [hour-ago-delayed (+ hour-ago 7)
                  date (str hour-ago-delayed " hours ago")
                  date-ago (sh "date" "-u" (str "--date=" date) "+%Y-%m-%d-%H")
                  out-file (format "%s/%s-UTC.tsv.gz" log-dir date-ago)]
              (sh
               "curl" "--silent" "--no-include" "--output" out-file
               "--location" "--header" pt-header
               (str "https://papertrailapp.com/api/v1/archives/" date-ago "/download")))))

        getMockData
        (do
          (sh "wget"
              #_"https://coronavirus-tracker-api.herokuapp.com/all"
              #_"https://covid-tracker-us.herokuapp.com/all"
              ((comp
                (partial format "https://%s/all")
                #_first
                second)
               env/api-servers)
              "-O" "resources/mockup/all.json")
          (sh "wget" env/owid-prod
              "-O" "resources/mockup/owid-covid-data.json"))

        deleteWebhook
        (sh "curl" "--form" "'drop_pending_updates=true'" "--request" "POST"
            (format "https://api.telegram.org/bot%s/%s"
                    deleteWebhook telegram-token))

        setWebhook
        (sh "curl"
            "--form" (format "'url=https://%s.herokuapp.com/%s'"
                             heroku-app telegram-token)
            "--form" "'drop_pending_updates=true'" "--request" "POST"
            (format "https://api.telegram.org/bot%s/%s"
                    setWebhook telegram-token))

        users
        ;; TODO prohibit sh-heroku from writing to stdout
        ((comp
          (fn [s] (cstr/split s #"\n")))
         (sh-heroku heroku-app "pt" (format ":type -ssl-client-cert -%s"
                                            (System/getenv "MY_TELEGRAM_ID"))))))))
