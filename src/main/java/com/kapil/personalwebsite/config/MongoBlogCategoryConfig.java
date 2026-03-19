package com.kapil.personalwebsite.config;

import com.kapil.personalwebsite.entity.BlogCategory;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

@Configuration
public class MongoBlogCategoryConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new StringToBlogCategoryConverter(),
                new BlogCategoryToStringConverter()
        ));
    }

    @ReadingConverter
    static class StringToBlogCategoryConverter implements Converter<String, BlogCategory> {
        @Override
        public BlogCategory convert(@NonNull String source) {
            return BlogCategory.fromStoredValue(source);
        }
    }

    @WritingConverter
    static class BlogCategoryToStringConverter implements Converter<BlogCategory, String> {
        @Override
        public String convert(BlogCategory source) {
            return source.name();
        }
    }

}
