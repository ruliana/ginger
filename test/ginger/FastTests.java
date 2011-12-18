package ginger;

import ginger.categories.SlowTest;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Categories.class)
@ExcludeCategory(SlowTest.class)
@SuiteClasses({ DuckTypeTest.class, RegexTest.class, SeqTest.class })
public class FastTests {}
