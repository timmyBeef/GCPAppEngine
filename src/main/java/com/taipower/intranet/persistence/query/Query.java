package com.taipower.intranet.persistence.query;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Query 物件，包含查詢字串及相關的參數。
 */
@Data
public final class Query {

    // .....................................................................[a-zA-Z_0-9]+
    private static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile("\\w+");

    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("'(?:[^']|'')*'");

    private static final Pattern PARAMETER_PATTERN = Pattern.compile(":(" + PARAMETER_NAME_PATTERN.pattern() + ")");

    private static final Pattern COMMENT_PATTERN = Pattern.compile("--.*");

    private static final Pattern STRING_TOKENIZER_PATTERN =
            Pattern.compile("(?<stringLiteral>" + STRING_LITERAL_PATTERN + ")" + "|" +
                    "(?::(?<parameterName>" + PARAMETER_NAME_PATTERN + "))" + "|" +
                    "(?<comment>(" + COMMENT_PATTERN + ")\n*)" + "|" +
                    "(?<rest>((?!(--|:|')).)+)");

    private final String string;
    private final Map<String, Object> parameters;

    /**
     * 建立 Builder 物件。
     * 
     * @return Builder 物件，可以繼續帶參數什麼的。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 指定 Query String 建立 Builder 物件。
     * 
     * @param string
     *            SQL 語句，可以帶 Named Parameters (例如:
     *            {@code "SELECT ID FROM A_TABLE WHERE TYPE = :type"} )。
     * @param parameters
     *            對應上述 SQL 語句的參數，數目一定要跟 SQL 中的參數一樣多，如果有相同的參數也請一樣依出現順序重複給值。<br>
     *            為了向前相容，也可以先不指定，後續再用其他方式代入。
     * @return Builder 物件，可以繼續帶參數什麼的。
     */
    public static Builder builder(@NotNull CharSequence string, Object... parameters) {
        return new Builder().append(string, parameters);
    }


    /**
     * 指定 SQL 語句及參數，直接建立 Query 物件。
     * 
     * @param queryString
     *            SQL 語句。
     * @param parameters
     *            參數集。
     * @return 依據給定參數新建立的 Query 物件。
     */
    public static Query of(@NotNull CharSequence queryString, Map<String, ?> parameters) {
        return new Query(queryString, parameters);
    }

    /**
     * 指定 SQL 語句，直接建立不帶參數的 Query 物件。
     * 
     * @param queryString
     *            SQL 語句。
     * @return 依據給定 SQL 語句新建立的 Query 物件。
     */
    public static Query of(@NotNull CharSequence queryString) {
        return new Query(queryString);
    }

    /**
     * 指定 SQL 語句及參數，自動將參數綁定進 SQL 語句中建立 Query 參數。
     * <p>
     * 例如:
     * 
     * <pre>
     * Query.of(&quot;SELECT A, B, C FROM T WHERE D BETWEEN :s AND :e&quot;, startDate, endDate);
     * </pre>
     * 
     * 將會產出參數集是 <code>{"s"=startDate, "e"=endDate}</code> 的 Query 物件。
     * <p>
     * 如果有比較複雜的 Query 需求 (例如有多個判斷式決定要不要帶什麼內容) 時，請使用 {@link #builder()}，透過 {@link Builder} 物件間接建立
     * Query 物件。
     * 
     * @param queryString
     *            SQL 語句。
     * @param parameters
     *            依序對應 SQL 語句參數的參數集。
     * @return 依據給定參數自動綁定所建立的 Query 物件。
     */
    public static Query of(@NotNull CharSequence queryString, Object... parameters) {
        return new Builder().append(queryString, parameters).build();
    }

    Query(@NotNull CharSequence string) {
        this(string, null);
    }

    Query(@NotNull CharSequence string, Map<String, ?> parameters) {
        if (string == null || Strings.isNullOrEmpty(string.toString())) {
            throw new IllegalArgumentException("Parameter \"string\" must not be null or empty.");
        }

        this.string = string.toString();
        if (parameters == null) {
            this.parameters = ImmutableMap.of();
        } else {
            // XXX Allows null value ?? Otherwise we can use ImmutableMap!
            this.parameters = new LinkedHashMap<>(parameters);
        }

        assertAllParametersProvided();
    }

    private void assertAllParametersProvided() {
        List<String> unresolveds = new ArrayList<>();

        Set<String> parameterNames = new LinkedHashSet<>();
        extractAndPopulateParameterNames(string, parameterNames);
        for (String parameterName : parameterNames) {
            if (!parameters.containsKey(parameterName)) {
                unresolveds.add(parameterName);
            }
        }

        if (!unresolveds.isEmpty()) {
            String message = String
                    .format("Parameter(s) %1$s are unresolvable in query string \"%2$s\" from given parameters: %3$s",
                            unresolveds, string, parameters);
            throw new IllegalArgumentException(message);
        }
    }

    private static List<String> extractAndPopulateParameterNames(CharSequence queryString,
                                                                 Collection<String> container) {

        List<String> tokenizedQuery = new ArrayList<>();

        Matcher matcher = STRING_TOKENIZER_PATTERN.matcher(queryString);
            StringBuilder token = new StringBuilder();
        while (matcher.find()) {
            String parameterName = matcher.group("parameterName");
            String stringLiteral = matcher.group("stringLiteral");
            String comment = matcher.group("comment");
            String rest = matcher.group("rest");

            if (parameterName != null) {
                container.add(parameterName);

                tokenizedQuery.add(token.toString());
                token = new StringBuilder();

            } else if (stringLiteral != null){
                token.append(stringLiteral);

            } else if (comment != null) {
                token.append(comment);

            } else {
                token.append(rest);
            }
        }

        if (token.length() != 0) {
            tokenizedQuery.add(token.toString());
        }

        return tokenizedQuery;
    }

    /**
     * 取得 SQL 語句。
     *
     * @return SQL 語句。
     */
    public String getString() {
        return string;
    }

    /**
     * 取得參數 Map。
     *
     * @return 參數 Map。
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * 協助組合 SQL 字串和代入參數，產生 Query 物件的工具。
     * <p>
     * 你可以簡單的想成是 {@code StringBuilder} 跟 {@code Map} 的合體。
     */
    public static class Builder {

        /** 是否自動展開 {@code Iterable} 參數的預設值，是 {@value} 。 */
        public static final boolean EXPAND_ITERABLE_PARAMETERS_DEFAULT = true;

        /** 是否自動在每兩句 SQL 語句片段中間補空格的預設值，是 {@value} 。 */
        public static final boolean ADD_SPACE_BETWEEN_APPENDS_DEFAULT = true;

        private static final char SPACE = ' ';

        /** SQL 字串。 */
        /* TODO private */final StringBuilder string = new StringBuilder();

        /** 查詢參數。 */
        /* TODO private */Map<String, Object> parameters = new LinkedHashMap<>();

        private boolean expandIterableParameters = EXPAND_ITERABLE_PARAMETERS_DEFAULT;
        private boolean addSpaceBetweenAppends = ADD_SPACE_BETWEEN_APPENDS_DEFAULT;

        /**
         * 設定是否自動展開 {@code Iterable} 參數。預設值為 {@link #EXPAND_ITERABLE_PARAMETERS_DEFAULT} 的值
         * {@value #EXPAND_ITERABLE_PARAMETERS_DEFAULT}。
         * <p>
         * 若設定成 {@code true}，則丟入的參數型態為 {@code Iterable} (如 {@code List} 或 {@code Set}) 時，會自動將 SQL
         * 語句中的參數展開，例如 {@code IN (:a)} 傳入 {@code [1, 4, 7]} 時，會自動將 SQL 語句轉換成
         * {@code IN (:a1, :a2, :a3)}，參數集展開成 <code>{"a1"=1, "a2"=4, "a3"=7}</code>。
         * 
         * @param expandIterableParameters
         *            {@code true} 代表要自動展開 {@code Iterable} 參數; {@code false} 則不要自動展開。
         * @return Builder 物件本身，方便繼續串其他參數。
         */
        public Builder expandIterableParameters(boolean expandIterableParameters) {
            this.expandIterableParameters = expandIterableParameters;
            return this;
        }

        /**
         * 取得是否自動展開 {@code Iterable} 參數的設定值。
         * <p>
         * 預設值為 {@link #EXPAND_ITERABLE_PARAMETERS_DEFAULT} 的值
         * {@value #EXPAND_ITERABLE_PARAMETERS_DEFAULT}。也可以透過
         * {@link #expandIterableParameters(boolean)} 設定。
         * 
         * @return 回傳 {@code true} 代表會自動展開 {@code Iterable} 參數; 否則回傳 {@code false}。
         */
        public boolean isExpandIterableParameters() {
            return expandIterableParameters;
        }

        /**
         * 設定是否自動在每兩句 SQL 語句片段中間補空格。預設值為 {@link #ADD_SPACE_BETWEEN_APPENDS_DEFAULT} 的值
         * {@value #ADD_SPACE_BETWEEN_APPENDS_DEFAULT}。
         * <p>
         * 若設定成 {@code true}，則每次 {@link #append(CharSequence, Object...)} 之間，都會自動判斷是否有空格 (" ")
         * 存在，如果沒有則自動補入一個空格; 設成 {@code false} 則不啟用本功能。
         * 
         * @param addSpaceBetweenAppends
         *            {@code true} 代表要自動在每兩句 SQL 語句片段中間補空格; {@code false} 則不要自動補。
         * @return Builder 物件本身，方便繼續串其他參數。
         */
        public Builder addSpaceBetweenAppends(boolean addSpaceBetweenAppends) {
            this.addSpaceBetweenAppends = addSpaceBetweenAppends;
            return this;
        }

        /**
         * 取得自動在每兩句 SQL 語句片段中間補空格的設定值。
         * <p>
         * 預設值為 {@link #ADD_SPACE_BETWEEN_APPENDS_DEFAULT} 的值
         * {@value #ADD_SPACE_BETWEEN_APPENDS_DEFAULT}。也可以透過
         * {@link #addSpaceBetweenAppends(boolean)} 設定。
         * 
         * @return 回傳 {@code true} 代表會自動展開 {@code Iterable} 參數; 否則回傳 {@code false}。
         */
        public boolean isAddSpaceBetweenAppends() {
            return addSpaceBetweenAppends;
        }

        void assertParameterNameValid(String parameterName) {
            if (!Query.PARAMETER_NAME_PATTERN.matcher(parameterName).matches()) {
                String message = String
                        .format("Parameter name \"%1$s\" is invalid.", parameterName);
                throw new IllegalArgumentException(message);
            }
        }

        /**
         * 如果 {code test} 參數結果值為 {@code true}，則做 {@link #append(CharSequence, Object...)} 的事;
         * 否則什麼事都不做。
         * <p>
         * 例如:
         * 
         * <pre>
         * builder.appendWhen(status == 3, &quot; AND TOTAL &gt;= :t&quot;, threshold);
         * </pre>
         * 
         * 則只有在 {@code status == 3} 成立時，才會將後面的 SQL 語句及參數帶入。
         * <p>
         * 講簡單一點，就等於是原本下面這樣子的寫法，但是只要一行就能搞定:
         * 
         * <pre>
         * if (status == 3) {
         *     builder.append(&quot; AND TOTAL &gt;= :t&quot;, threshold);
         * }
         * </pre>
         * 
         * @param test
         *            測試條件，只有條件成立時，才串後面的 SQL 語句及參數。
         * @param queryString
         *            SQL 語句，可以帶 Named Parameters (例如:
         *            {@code "SELECT ID FROM A_TABLE WHERE TYPE = :type"} )。
         * @param parameters
         *            對應上述 SQL 語句的參數，數目一定要跟 SQL 中的參數一樣多，如果有相同的參數也請一樣依出現順序重複給值。<br>
         *            參數型態如為 {@code Iterable}，且 {@code expandCollectionParameter} 值設為 {@code true}
         *            時會自動將該集合及 SQL 語句中的參數展開。<br>
         *            為了向前相容，也可以先不指定，後續再用其他方式代入。
         * @return Builder 物件本身，方便繼續串其他參數。
         */
        public Builder appendWhen(boolean test, CharSequence queryString, Object... parameters) {
            if (test) {
                return append(queryString, parameters);
            } else {
                return this;
            }
        }

        /**
         * 串接 SQL 字串，同時可以字串中對應的參數代入。可以多次呼叫，會逐一將 SQL 串出來。
         * <p>
         * 如果 {@link #expandIterableParameters(boolean)} 的值設為 {@code true}，則丟入的參數型態為
         * {@code Iterable} (如 {@code List} 或 {@code Set}) 時，會自動將 SQL 語句中的參數展開，例如 {@code IN (:a)} 傳入
         * {@code [1, 4, 7]} 時，會自動將 SQL 語句轉換成 {@code IN (:a1, :a2, :a3)}，參數集展開成
         * <code>{"a1"=1, "a2"=4, "a3"=7}</code>。
         * <p>
         * 請特別注意，<strong>所有由外部傳入的參數都請一定要透過本物件提供的方法代入，<span style="color: firebrick;">千萬不要自已將值展開填到
         * SQL 字串中，不然會有 SQL Injection 的風險。</span></strong>
         * 
         * @param queryString
         *            SQL 語句，可以帶 Named Parameters (例如:
         *            {@code "SELECT ID FROM A_TABLE WHERE TYPE = :type"} )。
         * @param parameters
         *            對應上述 SQL 語句的參數，數目一定要跟 SQL 中的參數一樣多，如果有相同的參數也請一樣依出現順序重複給值。<br>
         *            參數型態如為 {@code Iterable}，且 {@code expandCollectionParameter} 值設為 {@code true}
         *            時會自動將該集合及 SQL 語句中的參數展開。<br>
         *            為了向前相容，也可以先不指定，後續再用其他方式代入。
         * @return Builder 物件本身，方便繼續串其他參數。
         */
        public Builder append(CharSequence queryString, Object... parameters) {
            requireNonNull(queryString, "Parameter \"queryString\" must not be null.");

            if (isAddSpaceBetweenAppends()) {
                if (this.string.length() > 0 && queryString.length() > 0
                        && queryString.charAt(0) != SPACE) {
                    if (this.string.charAt(this.string.length() - 1) != SPACE) {
                        // 如果上一句最後一個字元跟這一句第一個字元都不是空白，就自動補一個空白
                        this.string.append(SPACE);
                    }
                }
            }

            // 如果有給定參數，就從查詢字串中抓出參數名稱，並一一將參數代入 Map
            if (parameters != null && parameters.length > 0) {
                appendAndPutParameters(this.string, queryString, parameters);

            } else {
                this.string.append(queryString);
            }
            return this;
        }

        private void appendAndPutParameters(StringBuilder queryString, CharSequence queryFragment,
                Object... parameters) {
            List<String> parameterNames = new ArrayList<>();
            Iterator<String> nonParameterParts = extractAndPopulateParameterNames(queryFragment, parameterNames).iterator();

            if (parameterNames.size() != parameters.length) {
                String message = String
                        .format("Query string \"%1$s\" contains %2$d parameter(s) %3$s,"
                                        + " and the size was mismatched to the given parameter(s) of %4$d containing %5$s",
                                queryFragment, parameterNames.size(), parameterNames,
                                parameters.length, Arrays.deepToString(parameters));
                throw new IllegalArgumentException(message);
            }

            queryString.append(nonParameterParts.next());

            for (int i = 0, n = parameterNames.size(); i < n; i++) {
                String currentParameterName = parameterNames.get(i);
                Object currentParameters = parameters[i];
                if (isExpandIterableParameters() && currentParameters instanceof Iterable) {
                    Iterable<?> values = (Iterable<?>) currentParameters;
                    expandIterable(queryString, currentParameterName, values);

                } else {
                    queryString.append(":").append(currentParameterName);
                    put(currentParameterName, currentParameters);
                }
                if (nonParameterParts.hasNext()) {
                    queryString.append(nonParameterParts.next());
                }
            }
        }

        private void expandIterable(StringBuilder queryString, String namePrefix, Iterable<?> values) {
            List<String> names = new ArrayList<>();
            // 從 1 開始，方便大家數數兒
            int counter = 1;
            for (Object value : values) {
                String name = namePrefix + counter;
                names.add(":" + name);
                put(name, value);
                counter++;
            }

            Joiner.on(", ").appendTo(queryString, names);
        }

        /**
         * 新增或取代參數。
         * 
         * @param parameterName
         *            參數名稱。
         * @param parameterValue
         *            參數值。
         * @return Builder 物件本身，方便繼續串其他參數。
         */
        public Builder put(String parameterName, Object parameterValue) {
            assertParameterNameValid(parameterName);

            parameters.put(parameterName, parameterValue);
            return this;
        }

        /**
         * 新增或取代整批參數。
         * 
         * @param parameters
         *            參數集合名稱。
         * @return Builder 物件本身，方便繼續串其他參數。
         */
        public Builder putAll(Map<String, ?> parameters) {
            for (Entry<String, ?> parameter : parameters.entrySet()) {
                put(parameter.getKey(), parameter.getValue());
            }

            return this;
        }

        /**
         * 建立 Query 物件。
         * 
         * @return 可供執行的 Query 物件。
         */
        public Query build() {
            return new Query(string, parameters);
        }
    }
}
