int
old_answer(void)
{
    return 41;
}

int
new_answer(void)
{
    return 42;
}

__asm__(".symver old_answer,answer@VERS_1.0");
__asm__(".symver new_answer,answer@@VERS_1.1");
