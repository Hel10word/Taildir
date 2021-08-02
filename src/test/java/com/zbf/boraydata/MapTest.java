package com.zbf.boraydata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author bufan
 * @data 2021/7/30
 */
public class MapTest {
    static class Age{
        private Integer age;

        @Override
        public String toString() {
            return "Age{" +
                    "age=" + age +
                    '}';
        }

        public Age(Integer age) {
            this.age = age;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }


    public static void main(String[] args) {
        Map<String,Age> map = new HashMap<>();
        map.put("1",new Age(1));
        map.put("2",new Age(2));
        map.put("3",new Age(3));
        map.put("4",new Age(4));
        updateMap(map);
        System.out.println(map.toString());
    }
    public static void updateMap(Map<String,Age> map){
        for(Iterator<Map.Entry<String,Age>> iterator = map.entrySet().iterator();iterator.hasNext();) {
            Map.Entry<String,Age> item = iterator.next();
            String key = item.getKey();
            Age age = map.get(key);
            if ("3".equals(key)) {
                iterator.remove();
            } else {
                age.setAge(age.getAge()+1);
                map.put(key,age);
            }
        }
    }
}
