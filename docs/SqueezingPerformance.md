# Squeezing Performance

Performance should already be good enough for 99% of people, but to squeeze out more, there are some tricks. This is
only of importance if your function is being called thousands to millions of times a second

The main overhead is in the conversion from Java types to native types, ie going to and from the native world

## Use Annotations such as @In and @Out

## @IgnoreError

## Load Library as early as possible

## Use smaller libraries if possible

## Add only the functions you need from the native library

## Use Pointer Over Specific Types

## Only call to native when absolutely necessary