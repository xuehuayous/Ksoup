package com.kevin.ksoup

import com.kevin.ksoup.annontation.Pick
import com.kevin.ksoup.extractor.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.reflect.Field

/**
 * Ksoup
 *
 * @author zwenkai@foxmail.com, Created on 2020-11-20 17:37:57
 *         Major Function：<b>Parsing HTML to object</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
class Ksoup {

    /**
     * This method deserializes the specified html into an object of the specified class.
     *
     * @param T     the type of the desired object
     * @param html  the string from which the object is to be deserialized
     * @param clazz the class of T
     * @return  an object of type T from the string.
     */
    fun <T : Any> parse(html: String, clazz: Class<T>): T {
        return parse(Jsoup.parse(html), clazz)
    }

    /**
     * This method deserializes the specified html into an object of the specified class.
     *
     * @param T         the type of the desired object
     * @param document  the document from which the object is to be deserialized
     * @param clazz     the class of T
     * @return  an object of type T from the string.
     */
    fun <T : Any> parse(document: Document, clazz: Class<T>): T {
        val rootNode = getRootNode(document, clazz)
        val obj: T
        try {
            obj = clazz.getConstructor().newInstance()
        } catch (e: NoSuchMethodException) {
            throw KsoupException("No-args constructor for class $clazz does not exist.", e)
        } catch (e: Exception) {
            throw KsoupException(e)
        }
        rootNode?.let {
            clazz.declaredFields.forEach { field ->
                getFieldValue(rootNode, obj, field)
            }
        }
        return obj
    }

    /**
     * Find root element that match the CSS query which declared in the [Pick].
     *
     * @param document  document to parse
     * @param clazz     the target class
     * @return the matching element, or <b>null</b> if none.
     */
    private fun getRootNode(document: Document, clazz: Class<*>): Element? {
        val pickClazz = clazz.getAnnotation(Pick::class.java)
        val cssQuery = pickClazz.value
        return document.selectFirst(cssQuery)
    }

    /**
     * Parsing HTML to assign values to the specified object field.
     *
     * @param node  the element
     * @param obj   the object
     * @param field the target field
     */
    internal fun getFieldValue(node: Element, obj: Any, field: Field) {
        field.isAccessible = true
        val defVal = field[obj]
        when (field.type) {
            Int::class.java -> field[obj] =
                IntTypeExtractor.extract(node, field, defVal as Int?, this)
            Long::class.java -> field[obj] =
                LongTypeExtractor.extract(node, field, defVal as Long?, this)
            Float::class.java -> field[obj] =
                FloatTypeExtractor.extract(node, field, defVal as Float?, this)
            String::class.java -> field[obj] =
                StringTypeExtractor.extract(node, field, defVal as String?, this)
            Double::class.java -> field[obj] =
                DoubleTypeExtractor.extract(node, field, defVal as Double?, this)
            Boolean::class.java -> field[obj] =
                BooleanTypeExtractor.extract(node, field, defVal as Boolean?, this)
            List::class.java -> field[obj] =
                ArrayTypeExtractor.extract(node, field, defVal as ArrayList<*>?, this)
            else -> throw KsoupException("Type ${field.type} is not supported.")
        }
    }

}