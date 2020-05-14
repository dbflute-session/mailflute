package org.dbflute.mail;

import java.util.HashMap;
import java.util.Map;

import org.dbflute.utflute.core.PlainTestCase;

/**
 * @author jflute
 * @since 0.6.2 (2020/05/14 Thursday at rjs)
 */
public class DeliveryCategoryTest extends PlainTestCase {

    public void test_equals() {
        // ## Arrange ##
        DeliveryCategory sea1 = new DeliveryCategory("sea");
        DeliveryCategory sea2 = new DeliveryCategory("sea");

        // ## Act ##
        // ## Assert ##
        assertEquals(sea1, sea2);
        assertNotSame(sea1, new DeliveryCategory("land"));
    }

    public void test_hashCode() {
        // ## Arrange ##
        DeliveryCategory sea1 = new DeliveryCategory("sea");
        DeliveryCategory sea2 = new DeliveryCategory("sea");

        // ## Act ##
        // ## Assert ##
        Map<DeliveryCategory, Object> map = new HashMap<>();
        map.put(sea1, new Object());
        assertNotNull(map.get(sea1));
        assertNotNull(map.get(sea2));
        assertNotSame(sea1, new DeliveryCategory("land"));
    }
}
