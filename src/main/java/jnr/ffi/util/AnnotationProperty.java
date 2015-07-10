package jnr.ffi.util;

/*
 *    Copyright 2010 The Miyamoto Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.util.Arrays;

/**
 * Describes an annotation property.
 *
 * @version $Id$
 */
final class AnnotationProperty {

    /**
     * The property name.
     */
    private final String name;

    /**
     * The property type.
     */
    private final Class<?> type;

    /**
     * The property value. This field can be mutable.
     */
    private Object value;

    /**
     * Creates a new annotation property instance.
     *
     * @param name the property name.
     * @param type the property type.
     */
    public AnnotationProperty(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the property name.
     *
     * @return the property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the property type.
     *
     * @return the property type.
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Returns the property value.
     *
     * @return the property value.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Sets the property value.
     *
     * @param value the property value.
     */
    public void setValue(Object value) {
        if (value != null && !(this.type.isAssignableFrom(value.getClass())
                || (this.type == Boolean.TYPE && value.getClass() == Boolean.class)
                || (this.type == Byte.TYPE && value.getClass() == Byte.class)
                || (this.type == Character.TYPE && value.getClass() == Character.class)
                || (this.type == Double.TYPE && value.getClass() == Double.class)
                || (this.type == Float.TYPE && value.getClass() == Float.class)
                || (this.type == Integer.TYPE && value.getClass() == Integer.class)
                || (this.type == Long.TYPE && value.getClass() == Long.class)
                || (this.type == Short.TYPE && value.getClass() == Short.class))) {
            throw new IllegalArgumentException("Cannot assign value of type '"
                    + value.getClass().getName()
                    + "' to property '"
                    + this.name
                    + "' of type '"
                    + this.type.getName()
                    + "'");
        }
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.name.hashCode();
        result = prime * result + this.type.hashCode();
        result = prime * result + this.getValueHashCode();
        return result;
    }

    /**
     * Calculates this annotation value hash code.
     *
     * @return this annotation value hash code.
     */
    protected int getValueHashCode() {
        if (this.value == null) {
            return 0;
        }

        if (!this.type.isArray()) {
            return this.value.hashCode();
        }

        if (this.type == byte[].class) {
            return Arrays.hashCode((byte[]) this.value);
        }
        if (this.type == char[].class) {
            return Arrays.hashCode((char[]) this.value);
        }
        if (this.type == double[].class) {
            return Arrays.hashCode((double[]) this.value);
        }
        if (this.type == float[].class) {
            return Arrays.hashCode((float[]) this.value);
        }
        if (this.type == int[].class) {
            return Arrays.hashCode((int[]) this.value);
        }
        if (this.type == long[].class) {
            return Arrays.hashCode((long[]) this.value);
        }
        if (this.type == short[].class) {
            return Arrays.hashCode((short[]) this.value);
        }
        if (this.type == boolean[].class) {
            return Arrays.hashCode((boolean[]) this.value);
        }
        return Arrays.hashCode((Object[]) this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        AnnotationProperty other = (AnnotationProperty) obj;

        if (this.name == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!this.name.equals(other.getName())) {
            return false;
        }

        if (this.type == null) {
            if (other.getType() != null) {
                return false;
            }
        } else if (!this.type.equals(other.getType())) {
            return false;
        }

        if (this.value == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else {
            // Check for primitive, string, class, enum const, annotation
            if (!this.type.isArray()) {
                return this.value.equals(other.getValue());
            }

            // Check for array of string, class, enum const, annotation
            if (this.value instanceof Object[] && other.getValue() instanceof Object[]) {
                Arrays.equals((Object[]) this.value, (Object[]) other.getValue());
            }

            // Deal with array of primitives
            if (this.type == byte[].class) {
                return Arrays.equals((byte[]) this.value, (byte[]) other.getValue());
            }
            if (this.type == char[].class) {
                return Arrays.equals((char[]) this.value, (char[]) other.getValue());
            }
            if (this.type == double[].class) {
                return Arrays.equals((double[]) this.value, (double[]) other.getValue());
            }
            if (this.type == float[].class) {
                return Arrays.equals((float[]) this.value, (float[]) other.getValue());
            }
            if (this.type == int[].class) {
                return Arrays.equals((int[]) this.value, (int[]) other.getValue());
            }
            if (this.type == long[].class) {
                return Arrays.equals((long[]) this.value, (long[]) other.getValue());
            }
            if (this.type == short[].class) {
                return Arrays.equals((short[]) this.value, (short[]) other.getValue());
            }
            if (this.type == boolean[].class) {
                return Arrays.equals((boolean[]) this.value, (boolean[]) other.getValue());
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(name="
                + this.name
                + ", type="
                + (this.type.isArray() ? (this.type.getComponentType().getName() + "[]") : this.type.getName())
                + ", value="
                + this.valueToString()
                + ")";
    }

    /**
     * Calculates the {@code toString} of the property value.
     *
     * @return the {@code toString} of the property value.
     */
    protected String valueToString() {
        if (!this.type.isArray()) {
            return String.valueOf(this.value);
        }

        Class<?> arrayType = this.type.getComponentType();
        if (arrayType == Boolean.TYPE) {
            return Arrays.toString((boolean[]) this.value);
        } else if (arrayType == Byte.TYPE) {
            return Arrays.toString((byte[]) this.value);
        } else if (arrayType == Character.TYPE) {
            return Arrays.toString((char[]) this.value);
        } else if (arrayType == Double.TYPE) {
            return Arrays.toString((double[]) this.value);
        } else if (arrayType == Float.TYPE) {
            return Arrays.toString((float[]) this.value);
        } else if (arrayType == Integer.TYPE) {
            return Arrays.toString((int[]) this.value);
        } else if (arrayType == Long.TYPE) {
            return Arrays.toString((long[]) this.value);
        } else if (arrayType == Short.TYPE) {
            return Arrays.toString((short[]) this.value);
        }

        return Arrays.toString((Object[]) this.value);
    }

}
