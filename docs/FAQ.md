# Frequently Asked Questions

* [Why Use JNR-FFI?](#why-use-jnr-ffi)
* [Should I switch to JNR-FFI from the library I'm currently using?](#should-i-switch-to-jnr-ffi-from-the-library-im-currently-using)
* [I have a question, where should I ask it?](#i-have-a-question-where-should-i-ask-it)
* [I have an issue or feature request](#i-have-an-issue-or-feature-request)
* [How can I contribute to the project?](#how-can-i-contribute-to-the-project)

## Why Use JNR-FFI?

See [the dedicated page for this very question](WhyUseJNR.md)

## Should I switch to JNR-FFI from the library I'm currently using?

Short answer: ***Probably no***.

If you're already happy with whatever you're already using such as JNA, chances are, switching to JNR-FFI won't add very
much for you. The cost of switching from another library such as JNA (which is the closest project to JNR-FFI) to
JNR-FFI is not insignificant and increases with project size.

We think JNR-FFI does a lot of things better than JNA and has a lot to offer over it,
([you can see the comparisons here](ComparisonToSimilarProjects.md)) but you need to take the cost of migration into
account when thinking about this.

However, if performance is your highest priority *AND* writing manual JNI code is out of the question for you, then
switching to JNR-FFI is more valuable as JNR-FFI is the most performant no-JNI native interop tool we are aware of. You
can see more details about this in [the comparisons to similar projects here](ComparisonToSimilarProjects.md).

## I have a question, where should I ask it?

The best place to ask questions is on the [Github Discussions](https://github.com/jnr/jnr-ffi/discussions) as that
guarantees the highest chance that the maintainers will see your question.

[StackOverflow](https://stackoverflow.com/) is also a great place to ask questions, be sure to tag your questions with
the [`jnr-ffi`](https://stackoverflow.com/questions/tagged/jnr-ffi) tag.

## I have an issue or feature request

First, make sure that the issue isn't an already existing one by searching
the [Github issues](https://github.com/jnr/jnr-ffi/issues), don't forget to search for closed issues too in case it was
already solved and closed.

If no Github issue exists for your specific question then open an issue
on [Github issues](https://github.com/jnr/jnr-ffi/issues) with as much information and detail as possible. The more
information provided, the easier it will be to solve the issue.

## How can I contribute to the project?

We are welcome to contributions to the project and would appreciate your support!

Before sending a pull request, please file an issue ([see the previous section](#i-have-an-issue-or-feature-request))
with the details of what you have changed or are going to change. This way, your time is not wasted if the change does
not meet our goals, and we can discuss the changes in depth within the issue.

If you are adding new functionality, ensure that you add unit tests that will test the behavior of your new code and, if
it is a public API, add descriptive javadoc comments for the new code.