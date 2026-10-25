package com.example.typingtime.analyzer

import com.example.typingtime.model.ApostropheMode
import com.example.typingtime.model.HyphenMode
import java.util.regex.Pattern

object WordCounter {

    // Regex for URLs (http/https/www)
    private val URL_PATTERN = Pattern.compile("^(https?://\\S+|www\\.\\S+)", Pattern.CASE_INSENSITIVE)

    // Regex for Email addresses
    private val EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE)

    // Regex for Hashtags and Mentions
    private val HASHTAG_MENTION_PATTERN = Pattern.compile("^[#@][\\p{L}\\p{N}_]+")

    // Regex for Version and Decimal numbers (e.g., 2.5.1, 12.50, 3.14159)
    private val DECIMAL_OR_VERSION_PATTERN = Pattern.compile("^\\d+(?:\\.\\d+)+")

    // Regex for Pure integers
    private val INTEGER_PATTERN = Pattern.compile("^\\d+")

    /**
     * Splits text into words according to Unicode rules and user settings.
     */
    fun extractWords(
        text: String,
        countNumbers: Boolean = true,
        hyphenMode: HyphenMode = HyphenMode.KEEP_AS_ONE,
        apostropheMode: ApostropheMode = ApostropheMode.KEEP_AS_ONE,
        urlAsOneWord: Boolean = true,
        emailAsOneWord: Boolean = true
    ): List<String> {
        if (text.isBlank()) return emptyList()

        val words = mutableListOf<String>()
        val length = text.length
        var i = 0

        while (i < length) {
            val c = text[i]

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++
                continue
            }

            val remaining = text.substring(i)

            // 1. Check for URL
            if (urlAsOneWord) {
                val urlMatcher = URL_PATTERN.matcher(remaining)
                if (urlMatcher.find()) {
                    var url = urlMatcher.group(1) ?: ""
                    // Trim trailing punctuation marks that often cling to URLs at sentence ends
                    url = url.trimEnd('.', ',', '!', '?', ';', ':', ')', ']', '}', '"', '\'')
                    if (url.isNotEmpty()) {
                        words.add(url)
                        i += url.length
                        continue
                    }
                }
            }

            // 2. Check for Email
            if (emailAsOneWord) {
                val emailMatcher = EMAIL_PATTERN.matcher(remaining)
                if (emailMatcher.find()) {
                    var email = emailMatcher.group()
                    email = email.trimEnd('.', ',', '!', '?', ';', ':', ')', ']', '}', '"', '\'')
                    if (email.isNotEmpty()) {
                        words.add(email)
                        i += email.length
                        continue
                    }
                }
            }

            // 3. Check for Hashtag or Mention
            val tagMatcher = HASHTAG_MENTION_PATTERN.matcher(remaining)
            if (tagMatcher.find()) {
                val tag = tagMatcher.group()
                words.add(tag)
                i += tag.length
                continue
            }

            // 4. Check for Version numbers or Decimal numbers (e.g. 2.5.1, 12.50)
            val decMatcher = DECIMAL_OR_VERSION_PATTERN.matcher(remaining)
            if (decMatcher.find()) {
                val num = decMatcher.group()
                if (countNumbers) {
                    words.add(num)
                }
                i += num.length
                continue
            }

            // 5. Check for standalone integers
            val intMatcher = INTEGER_PATTERN.matcher(remaining)
            if (intMatcher.find()) {
                val num = intMatcher.group()
                // Ensure this is not followed by a letter (like in 45th or 2nd)
                val nextIndex = i + num.length
                if (nextIndex < length && Character.isLetter(text[nextIndex])) {
                    // Let the alphanumeric branch handle things like "25th"
                } else {
                    if (countNumbers) {
                        words.add(num)
                    }
                    i += num.length
                    continue
                }
            }

            // 6. Check for CJK Ideographs / Hiragana / Katakana
            val codePoint = Character.codePointAt(text, i)
            val charCount = Character.charCount(codePoint)
            if (isCjk(codePoint)) {
                words.add(String(Character.toChars(codePoint)))
                i += charCount
                continue
            }

            // 7. Check for word letters (Unicode letters and digits when alphanumeric)
            if (Character.isLetter(codePoint) || (countNumbers && Character.isDigit(codePoint))) {
                val wordBuilder = StringBuilder()
                var j = i

                while (j < length) {
                    val cp = Character.codePointAt(text, j)
                    val cc = Character.charCount(cp)

                    if (Character.isLetter(cp) || Character.isDigit(cp)) {
                        wordBuilder.appendCodePoint(cp)
                        j += cc
                    } else if (cp == '\''.code || cp == '’'.code) {
                        if (apostropheMode == ApostropheMode.KEEP_AS_ONE) {
                            // Only keep if inside word (followed by a letter)
                            val nextPos = j + cc
                            if (nextPos < length && Character.isLetter(Character.codePointAt(text, nextPos))) {
                                wordBuilder.appendCodePoint(cp)
                                j += cc
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    } else if (cp == '-'.code) {
                        if (hyphenMode == HyphenMode.KEEP_AS_ONE) {
                            // Only keep if inside word (followed by a letter or digit)
                            val nextPos = j + cc
                            if (nextPos < length && (Character.isLetter(Character.codePointAt(text, nextPos)) ||
                                        Character.isDigit(Character.codePointAt(text, nextPos)))) {
                                wordBuilder.appendCodePoint(cp)
                                j += cc
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }

                val word = wordBuilder.toString().trim()
                if (word.isNotEmpty()) {
                    // If numbers are disabled and the word consists only of digits, don't count
                    val isPureNumber = word.all { it.isDigit() }
                    if (!isPureNumber || countNumbers) {
                        words.add(word)
                    }
                }
                i = if (j > i) j else i + 1
                continue
            }

            // Skip punctuation or other symbols
            i += charCount
        }

        return words
    }

    fun countWords(
        text: String,
        countNumbers: Boolean = true,
        hyphenMode: HyphenMode = HyphenMode.KEEP_AS_ONE,
        apostropheMode: ApostropheMode = ApostropheMode.KEEP_AS_ONE,
        urlAsOneWord: Boolean = true,
        emailAsOneWord: Boolean = true
    ): Int {
        return extractWords(
            text = text,
            countNumbers = countNumbers,
            hyphenMode = hyphenMode,
            apostropheMode = apostropheMode,
            urlAsOneWord = urlAsOneWord,
            emailAsOneWord = emailAsOneWord
        ).size
    }

    private fun isCjk(codePoint: Int): Boolean {
        val block = Character.UnicodeBlock.of(codePoint)
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                block == Character.UnicodeBlock.HIRAGANA ||
                block == Character.UnicodeBlock.KATAKANA ||
                block == Character.UnicodeBlock.BOPOMOFO
    }
}
