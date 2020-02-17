/*
 * Copyright 2010-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.attribute;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.util.SdkAutoConstructList;
import software.amazon.awssdk.core.util.SdkAutoConstructMap;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.converter.TypeConvertingVisitor;
import software.amazon.awssdk.extensions.dynamodb.mappingclient.core.AttributeValueType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.utils.ToString;
import software.amazon.awssdk.utils.Validate;

/**
 * A simpler, and more user-friendly version of the generated {@link AttributeValue}.
 *
 * <p>
 * This is a union type of the types exposed by DynamoDB, exactly as they're exposed by DynamoDB.
 *
 * <p>
 * An instance of {@link ItemAttributeValue} represents exactly one DynamoDB type, like String (s), Number (n) or Bytes (b). This
 * type can be determined with the {@link #type()} method or the {@code is*} methods like {@link #isString()} or
 * {@link #isNumber()}. Once the type is known, the value can be extracted with {@code as*} methods like {@link #asString()}
 * or {@link #asNumber()}.
 *
 * <p>
 * When converting an {@link ItemAttributeValue} into a concrete Java type, it can be tedious to use the {@link #type()} or
 * {@code is*} methods. For this reason, a {@link #convert(TypeConvertingVisitor)} method is provided that exposes a polymorphic
 * way of converting a value into another type.
 *
 * <p>
 * An instance of {@link ItemAttributeValue} is created with the {@code from*} methods, like {@link #fromString(String)} or
 * {@link #fromNumber(String)}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class ItemAttributeValue {
    private final AttributeValueType type;
    private final boolean isNull;
    private final Map<String, ItemAttributeValue> mapValue;
    private final String stringValue;
    private final String numberValue;
    private final SdkBytes bytesValue;
    private final Boolean booleanValue;
    private final List<String> setOfStringsValue;
    private final List<String> setOfNumbersValue;
    private final List<SdkBytes> setOfBytesValue;
    private final List<ItemAttributeValue> listOfAttributeValuesValue;

    private ItemAttributeValue(InternalBuilder builder) {
        this.type = builder.type;
        this.isNull = builder.isNull;
        this.stringValue = builder.stringValue;
        this.numberValue = builder.numberValue;
        this.bytesValue = builder.bytesValue;
        this.booleanValue = builder.booleanValue;

        this.mapValue = builder.mapValue == null
                        ? null
                        : Collections.unmodifiableMap(new LinkedHashMap<>(builder.mapValue));
        this.setOfStringsValue = builder.setOfStringsValue == null
                                 ? null
                                 : Collections.unmodifiableList(new ArrayList<>(builder.setOfStringsValue));
        this.setOfNumbersValue = builder.setOfNumbersValue == null
                                 ? null
                                 : Collections.unmodifiableList(new ArrayList<>(builder.setOfNumbersValue));
        this.setOfBytesValue = builder.setOfBytesValue == null
                               ? null
                               : Collections.unmodifiableList(new ArrayList<>(builder.setOfBytesValue));
        this.listOfAttributeValuesValue = builder.listOfAttributeValuesValue == null
                                          ? null
                                          : Collections.unmodifiableList(new ArrayList<>(builder.listOfAttributeValuesValue));
    }

    /**
     * Create an {@link ItemAttributeValue} for the null DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().nul(true).build())}
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public static ItemAttributeValue nullValue() {
        return new InternalBuilder().isNull().build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a map (m) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().m(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided map is null or has null keys.
     */
    public static ItemAttributeValue fromMap(Map<String, ItemAttributeValue> mapValue) {
        Validate.paramNotNull(mapValue, "mapValue");
        Validate.noNullElements(mapValue.keySet(), "Map must not have null keys.");
        return new InternalBuilder().mapValue(mapValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a string (s) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().s(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null. Use {@link #nullValue()} for
     * null values.
     */
    public static ItemAttributeValue fromString(String stringValue) {
        Validate.paramNotNull(stringValue, "stringValue");
        return new InternalBuilder().stringValue(stringValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a number (n) DynamoDB type.
     *
     * <p>
     * This is a String, because it matches the underlying DynamoDB representation.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().n(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null. Use {@link #nullValue()} for
     * null values.
     */
    public static ItemAttributeValue fromNumber(String numberValue) {
        Validate.paramNotNull(numberValue, "numberValue");
        return new InternalBuilder().numberValue(numberValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a bytes (b) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().b(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null. Use {@link #nullValue()} for
     * null values.
     */
    public static ItemAttributeValue fromBytes(SdkBytes bytesValue) {
        Validate.paramNotNull(bytesValue, "bytesValue");
        return new InternalBuilder().bytesValue(bytesValue).build();
    }


    /**
     * Create an {@link ItemAttributeValue} for a boolean (bool) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().bool(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null. Use {@link #nullValue()} for
     * null values.
     */
    public static ItemAttributeValue fromBoolean(Boolean booleanValue) {
        Validate.paramNotNull(booleanValue, "booleanValue");
        return new InternalBuilder().booleanValue(booleanValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a set-of-strings (ss) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().ss(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #fromListOfAttributeValues(List)} for null values. This <i>will not</i> validate that there are no
     * duplicate values.
     */
    public static ItemAttributeValue fromSetOfStrings(String... setOfStringsValue) {
        Validate.paramNotNull(setOfStringsValue, "setOfStringsValue");
        return fromSetOfStrings(Arrays.asList(setOfStringsValue));
    }

    /**
     * Create an {@link ItemAttributeValue} for a set-of-strings (ss) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().ss(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #fromListOfAttributeValues(List)} for null values. This <i>will not</i> validate that there are no
     * duplicate values.
     */
    public static ItemAttributeValue fromSetOfStrings(Collection<String> setOfStringsValue) {
        Validate.paramNotNull(setOfStringsValue, "setOfStringsValue");
        Validate.noNullElements(setOfStringsValue, "Set must not have null values.");
        return new InternalBuilder().setOfStringsValue(setOfStringsValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a set-of-numbers (ns) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().ns(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #fromListOfAttributeValues(List)} for null values. This <i>will not</i> validate that there are no
     * duplicate values.
     */
    public static ItemAttributeValue fromSetOfNumbers(String... setOfNumbersValue) {
        Validate.paramNotNull(setOfNumbersValue, "setOfNumbersValue");
        return fromSetOfNumbers(Arrays.asList(setOfNumbersValue));
    }

    /**
     * Create an {@link ItemAttributeValue} for a set-of-numbers (ns) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().ns(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #fromListOfAttributeValues(List)} for null values. This <i>will not</i> validate that there are no
     * duplicate values.
     */
    public static ItemAttributeValue fromSetOfNumbers(Collection<String> setOfNumbersValue) {
        Validate.paramNotNull(setOfNumbersValue, "setOfNumbersValue");
        Validate.noNullElements(setOfNumbersValue, "Set must not have null values.");
        return new InternalBuilder().setOfNumbersValue(setOfNumbersValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a set-of-bytes (bs) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().bs(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #fromListOfAttributeValues(List)} for null values. This <i>will not</i> validate that there are no
     * duplicate values.
     */
    public static ItemAttributeValue fromSetOfBytes(SdkBytes... setOfBytesValue) {
        Validate.paramNotNull(setOfBytesValue, "setOfBytesValue");
        return fromSetOfBytes(Arrays.asList(setOfBytesValue));
    }

    /**
     * Create an {@link ItemAttributeValue} for a set-of-bytes (bs) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().bs(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #fromListOfAttributeValues(List)} for null values. This <i>will not</i> validate that there are no
     * duplicate values.
     */
    public static ItemAttributeValue fromSetOfBytes(Collection<SdkBytes> setOfBytesValue) {
        Validate.paramNotNull(setOfBytesValue, "setOfBytesValue");
        Validate.noNullElements(setOfBytesValue, "Set must not have null values.");
        return new InternalBuilder().setOfBytesValue(setOfBytesValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} for a list-of-attributes (l) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().l(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #nullValue()} for null values.
     */
    public static ItemAttributeValue fromListOfAttributeValues(ItemAttributeValue... listOfAttributeValuesValue) {
        Validate.paramNotNull(listOfAttributeValuesValue, "listOfAttributeValuesValue");
        return fromListOfAttributeValues(Arrays.asList(listOfAttributeValuesValue));
    }

    /**
     * Create an {@link ItemAttributeValue} for a list-of-attributes (l) DynamoDB type.
     *
     * <p>
     * Equivalent to: {@code ItemAttributeValue.fromGeneratedAttributeValue(AttributeValue.builder().l(...).build())}
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null or contains a null value. Use
     * {@link #nullValue()} for null values.
     */
    public static ItemAttributeValue fromListOfAttributeValues(List<ItemAttributeValue> listOfAttributeValuesValue) {
        Validate.paramNotNull(listOfAttributeValuesValue, "listOfAttributeValuesValue");
        Validate.noNullElements(listOfAttributeValuesValue, "List must not have null values.");
        return new InternalBuilder().listOfAttributeValuesValue(listOfAttributeValuesValue).build();
    }

    /**
     * Create an {@link ItemAttributeValue} from a generated {@code Map<String, AttributeValue>}.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided map is null or contains a null key or value. Use
     * {@link AttributeValue#nul()} for null values.
     */
    public static ItemAttributeValue fromGeneratedItem(Map<String, AttributeValue> attributeValues) {
        Validate.paramNotNull(attributeValues, "attributeValues");
        Map<String, ItemAttributeValue> result = new LinkedHashMap<>();
        attributeValues.forEach((key, value) -> {
            Validate.notNull(key, "Generated item must not contain null keys.");
            result.put(key, fromGeneratedAttributeValue(value));
        });
        return ItemAttributeValue.fromMap(result);
    }

    /**
     * Create an {@link ItemAttributeValue} from a generated {@link AttributeValue}.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if the provided value is null ({@link AttributeValue#nul()} is okay).
     */
    public static ItemAttributeValue fromGeneratedAttributeValue(AttributeValue attributeValue) {
        Validate.notNull(attributeValue, "Generated attribute value must not contain null values. " +
                                         "Use AttributeValue#nul() instead.");
        if (attributeValue.s() != null) {
            return ItemAttributeValue.fromString(attributeValue.s());
        }
        if (attributeValue.n() != null) {
            return ItemAttributeValue.fromNumber(attributeValue.n());
        }
        if (attributeValue.bool() != null) {
            return ItemAttributeValue.fromBoolean(attributeValue.bool());
        }
        if (Boolean.TRUE.equals(attributeValue.nul())) {
            return ItemAttributeValue.nullValue();
        }
        if (attributeValue.b() != null) {
            return ItemAttributeValue.fromBytes(attributeValue.b());
        }
        if (attributeValue.m() != null && !(attributeValue.m() instanceof SdkAutoConstructMap)) {
            Map<String, ItemAttributeValue> map = new LinkedHashMap<>();
            attributeValue.m().forEach((k, v) -> map.put(k, ItemAttributeValue.fromGeneratedAttributeValue(v)));
            return ItemAttributeValue.fromMap(map);
        }
        if (attributeValue.l() != null && !(attributeValue.l() instanceof SdkAutoConstructList)) {
            List<ItemAttributeValue> list =
                    attributeValue.l().stream().map(ItemAttributeValue::fromGeneratedAttributeValue).collect(toList());
            return ItemAttributeValue.fromListOfAttributeValues(list);
        }
        if (attributeValue.bs() != null && !(attributeValue.bs() instanceof SdkAutoConstructList)) {
            return ItemAttributeValue.fromSetOfBytes(attributeValue.bs());
        }
        if (attributeValue.ss() != null && !(attributeValue.ss() instanceof SdkAutoConstructList)) {
            return ItemAttributeValue.fromSetOfStrings(attributeValue.ss());
        }
        if (attributeValue.ns() != null && !(attributeValue.ns() instanceof SdkAutoConstructList)) {
            return ItemAttributeValue.fromSetOfNumbers(attributeValue.ns());
        }

        throw new IllegalStateException("Unable to convert attribute value: " + attributeValue);
    }

    /**
     * Retrieve the underlying DynamoDB type of this value, such as String (s) or Number (n).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public AttributeValueType type() {
        return type;
    }

    /**
     * Apply the provided visitor to this item attribute value, converting it into a specific type. This is useful in
     * {@link AttributeConverter} implementations, without having to write a switch statement on the {@link #type()}.
     *
     * <p>
     * Reasons this call may fail with a {@link RuntimeException}:
     * <ol>
     *     <li>If the provided visitor is null.</li>
     *     <li>If the value cannot be converted by this visitor.</li>
     * </ol>
     */
    public <T> T convert(TypeConvertingVisitor<T> convertingVisitor) {
        Validate.paramNotNull(convertingVisitor, "convertingVisitor");
        return convertingVisitor.convert(this);
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a Map (m).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isMap() {
        return mapValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a String (s).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isString() {
        return stringValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a Number (n).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isNumber() {
        return numberValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is Bytes (b).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isBytes() {
        return bytesValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a Boolean (bool).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isBoolean() {
        return booleanValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a Set of Strings (ss).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isSetOfStrings() {
        return setOfStringsValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a Set of Numbers (ns).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isSetOfNumbers() {
        return setOfNumbersValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a Set of Bytes (bs).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isSetOfBytes() {
        return setOfBytesValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is a List of AttributeValues (l).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isListOfAttributeValues() {
        return listOfAttributeValuesValue != null;
    }

    /**
     * Returns true if the underlying DynamoDB type of this value is Null (null).
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public boolean isNull() {
        return isNull;
    }

    /**
     * Retrieve this value as a map.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isMap()} is false.
     */
    public Map<String, ItemAttributeValue> asMap() {
        Validate.isTrue(isMap(), "Value is not a map.");
        return mapValue;
    }

    /**
     * Retrieve this value as a string.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isString()} is false.
     */
    public String asString() {
        Validate.isTrue(isString(), "Value is not a string.");
        return stringValue;
    }

    /**
     * Retrieve this value as a number.
     *
     * Note: This returns a {@code String} (instead of a {@code Number}), because that's the generated type from
     * DynamoDB: {@link AttributeValue#n()}.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isNumber()} is false.
     */
    public String asNumber() {
        Validate.isTrue(isNumber(), "Value is not a number.");
        return numberValue;
    }

    /**
     * Retrieve this value as bytes.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isBytes()} is false.
     */
    public SdkBytes asBytes() {
        Validate.isTrue(isBytes(), "Value is not bytes.");
        return bytesValue;
    }

    /**
     * Retrieve this value as a boolean.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isBoolean()} is false.
     */
    public Boolean asBoolean() {
        Validate.isTrue(isBoolean(), "Value is not a boolean.");
        return booleanValue;
    }

    /**
     * Retrieve this value as a set of strings.
     *
     * <p>
     * Note: This returns a {@code List} (instead of a {@code Set}), because that's the generated type from
     * DynamoDB: {@link AttributeValue#ss()}.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isSetOfStrings()} is false.
     */
    public List<String> asSetOfStrings() {
        Validate.isTrue(isSetOfStrings(), "Value is not a list of strings.");
        return setOfStringsValue;
    }

    /**
     * Retrieve this value as a set of numbers.
     *
     * <p>
     * Note: This returns a {@code List<String>} (instead of a {@code Set<Number>}), because that's the generated type from
     * DynamoDB: {@link AttributeValue#ns()}.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isSetOfNumbers()} is false.
     */
    public List<String> asSetOfNumbers() {
        Validate.isTrue(isSetOfNumbers(), "Value is not a list of numbers.");
        return setOfNumbersValue;
    }

    /**
     * Retrieve this value as a set of bytes.
     *
     * <p>
     * Note: This returns a {@code List} (instead of a {@code Set}), because that's the generated type from
     * DynamoDB: {@link AttributeValue#bs()}.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isSetOfBytes()} is false.
     */
    public List<SdkBytes> asSetOfBytes() {
        Validate.isTrue(isSetOfBytes(), "Value is not a list of bytes.");
        return setOfBytesValue;
    }

    /**
     * Retrieve this value as a list of attribute values.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isListOfAttributeValues()} is false.
     */
    public List<ItemAttributeValue> asListOfAttributeValues() {
        Validate.isTrue(isListOfAttributeValues(), "Value is not a list of attribute values.");
        return listOfAttributeValuesValue;
    }

    /**
     * Convert this {@link ItemAttributeValue} into a generated {@code Map<String, AttributeValue>}.
     *
     * <p>
     * This call will fail with a {@link RuntimeException} if {@link #isMap()} is false.
     */
    public Map<String, AttributeValue> toGeneratedItem() {
        Validate.validState(isMap(), "Cannot convert an attribute value of type %s to a generated item. Must be %s.",
                            type(), AttributeValueType.M);

        AttributeValue generatedAttributeValue = toGeneratedAttributeValue();

        Validate.validState(generatedAttributeValue.m() != null && !(generatedAttributeValue.m() instanceof SdkAutoConstructMap),
                            "Map ItemAttributeValue was not converted into a Map AttributeValue.");
        return generatedAttributeValue.m();
    }

    /**
     * Convert this {@link ItemAttributeValue} into a generated {@link AttributeValue}.
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    public AttributeValue toGeneratedAttributeValue() {
        return convert(ToGeneratedAttributeValueVisitor.INSTANCE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemAttributeValue that = (ItemAttributeValue) o;

        if (isNull != that.isNull) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (mapValue != null ? !mapValue.equals(that.mapValue) : that.mapValue != null) {
            return false;
        }
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) {
            return false;
        }
        if (numberValue != null ? !numberValue.equals(that.numberValue) : that.numberValue != null) {
            return false;
        }
        if (bytesValue != null ? !bytesValue.equals(that.bytesValue) : that.bytesValue != null) {
            return false;
        }
        if (booleanValue != null ? !booleanValue.equals(that.booleanValue) : that.booleanValue != null) {
            return false;
        }
        if (setOfStringsValue != null ? !setOfStringsValue.equals(that.setOfStringsValue) : that.setOfStringsValue != null) {
            return false;
        }
        if (setOfNumbersValue != null ? !setOfNumbersValue.equals(that.setOfNumbersValue) : that.setOfNumbersValue != null) {
            return false;
        }
        if (setOfBytesValue != null ? !setOfBytesValue.equals(that.setOfBytesValue) : that.setOfBytesValue != null) {
            return false;
        }
        return listOfAttributeValuesValue != null ? listOfAttributeValuesValue.equals(that.listOfAttributeValuesValue)
                                                  : that.listOfAttributeValuesValue == null;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (isNull ? 1 : 0);
        result = 31 * result + (mapValue != null ? mapValue.hashCode() : 0);
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        result = 31 * result + (numberValue != null ? numberValue.hashCode() : 0);
        result = 31 * result + (bytesValue != null ? bytesValue.hashCode() : 0);
        result = 31 * result + (booleanValue != null ? booleanValue.hashCode() : 0);
        result = 31 * result + (setOfStringsValue != null ? setOfStringsValue.hashCode() : 0);
        result = 31 * result + (setOfNumbersValue != null ? setOfNumbersValue.hashCode() : 0);
        result = 31 * result + (setOfBytesValue != null ? setOfBytesValue.hashCode() : 0);
        result = 31 * result + (listOfAttributeValuesValue != null ? listOfAttributeValuesValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        Object value = convert(ToStringVisitor.INSTANCE);
        return ToString.builder("ItemAttributeValue")
                       .add("type", type)
                       .add("value", value)
                       .build();
    }

    private static class ToGeneratedAttributeValueVisitor extends TypeConvertingVisitor<AttributeValue> {
        private static final ToGeneratedAttributeValueVisitor INSTANCE = new ToGeneratedAttributeValueVisitor();

        private ToGeneratedAttributeValueVisitor() {
            super(AttributeValue.class);
        }

        @Override
        public AttributeValue convertNull() {
            return AttributeValue.builder().nul(true).build();
        }

        @Override
        public AttributeValue convertMap(Map<String, ItemAttributeValue> value) {
            Map<String, AttributeValue> map = new LinkedHashMap<>();
            value.forEach((k, v) -> map.put(k, v.toGeneratedAttributeValue()));
            return AttributeValue.builder().m(map).build();
        }

        @Override
        public AttributeValue convertString(String value) {
            return AttributeValue.builder().s(value).build();
        }

        @Override
        public AttributeValue convertNumber(String value) {
            return AttributeValue.builder().n(value).build();
        }

        @Override
        public AttributeValue convertBytes(SdkBytes value) {
            return AttributeValue.builder().b(value).build();
        }

        @Override
        public AttributeValue convertBoolean(Boolean value) {
            return AttributeValue.builder().bool(value).build();
        }

        @Override
        public AttributeValue convertSetOfStrings(List<String> value) {
            return AttributeValue.builder().ss(value).build();
        }

        @Override
        public AttributeValue convertSetOfNumbers(List<String> value) {
            return AttributeValue.builder().ns(value).build();
        }

        @Override
        public AttributeValue convertSetOfBytes(List<SdkBytes> value) {
            return AttributeValue.builder().bs(value).build();
        }

        @Override
        public AttributeValue convertListOfAttributeValues(List<ItemAttributeValue> value) {
            return AttributeValue.builder()
                                 .l(value.stream().map(ItemAttributeValue::toGeneratedAttributeValue).collect(toList()))
                                 .build();
        }
    }

    private static class ToStringVisitor extends TypeConvertingVisitor<Object> {
        private static final ToStringVisitor INSTANCE = new ToStringVisitor();

        private ToStringVisitor() {
            super(Object.class);
        }

        @Override
        public Object convertNull() {
            return "null";
        }

        @Override
        public Object defaultConvert(AttributeValueType type, Object value) {
            return value;
        }
    }

    private static class InternalBuilder {
        private AttributeValueType type;
        private boolean isNull = false;
        private Map<String, ItemAttributeValue> mapValue;
        private String stringValue;
        private String numberValue;
        private SdkBytes bytesValue;
        private Boolean booleanValue;
        private Collection<String> setOfStringsValue;
        private Collection<String> setOfNumbersValue;
        private Collection<SdkBytes> setOfBytesValue;
        private Collection<ItemAttributeValue> listOfAttributeValuesValue;

        public InternalBuilder isNull() {
            this.type = AttributeValueType.NULL;
            this.isNull = true;
            return this;
        }

        private InternalBuilder mapValue(Map<String, ItemAttributeValue> mapValue) {
            this.type = AttributeValueType.M;
            this.mapValue = mapValue;
            return this;
        }

        private InternalBuilder stringValue(String stringValue) {
            this.type = AttributeValueType.S;
            this.stringValue = stringValue;
            return this;
        }

        private InternalBuilder numberValue(String numberValue) {
            this.type = AttributeValueType.N;
            this.numberValue = numberValue;
            return this;
        }

        private InternalBuilder bytesValue(SdkBytes bytesValue) {
            this.type = AttributeValueType.B;
            this.bytesValue = bytesValue;
            return this;
        }

        private InternalBuilder booleanValue(Boolean booleanValue) {
            this.type = AttributeValueType.BOOL;
            this.booleanValue = booleanValue;
            return this;
        }

        private InternalBuilder setOfStringsValue(Collection<String> setOfStringsValue) {
            this.type = AttributeValueType.SS;
            this.setOfStringsValue = setOfStringsValue;
            return this;
        }

        private InternalBuilder setOfNumbersValue(Collection<String> setOfNumbersValue) {
            this.type = AttributeValueType.NS;
            this.setOfNumbersValue = setOfNumbersValue;
            return this;
        }

        private InternalBuilder setOfBytesValue(Collection<SdkBytes> setOfBytesValue) {
            this.type = AttributeValueType.BS;
            this.setOfBytesValue = setOfBytesValue;
            return this;
        }

        private InternalBuilder listOfAttributeValuesValue(Collection<ItemAttributeValue> listOfAttributeValuesValue) {
            this.type = AttributeValueType.L;
            this.listOfAttributeValuesValue = listOfAttributeValuesValue;
            return this;
        }

        private ItemAttributeValue build() {
            return new ItemAttributeValue(this);
        }
    }
}
