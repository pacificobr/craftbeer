package com.beerhouse.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.stream.Stream;

public class DataUtil extends BeanUtils {

    public static <T> T mergeObjects(T source, T destination){
        String[] nullProperties = getNullPropertyNames(source);
        BeanUtils.copyProperties(source, destination, nullProperties);
        return destination;
    }

    private  static <T> String[] getNullPropertyNames(T entity) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(entity);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

}