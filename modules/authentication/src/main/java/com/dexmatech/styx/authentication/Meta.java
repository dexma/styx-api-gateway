package com.dexmatech.styx.authentication;

import lombok.ToString;

/**
 * Created by gszeliga on 22/12/14.
 */
@ToString
public class Meta {

	//    public static final String DEPLOYMENT = "ACCESS_POLICY#DEPLOYMENT";
	//    public static final String ACCOUNT = "ACCESS_POLICY#ACCOUNT";
	//    public static final String APPLICATION = "ACCESS_POLICY#APPLICATION";
	//
	//    private final Map<String, Object> content = new HashMap<>();
	//
	//    public void put(String key, Object value)
	//    {
	//        checkArgument(key != null && key.trim().length() > 0,"Key value must be specified");
	//
	//        content.put(key, value);
	//    }
	//
	//    public Optional<Object> get(String key)
	//    {
	//        checkArgument(key != null && key.trim().length() > 0,"Key value must be specified");
	//
	//        return Optional.ofNullable(content.get(key));
	//    }
	//
	//    public Helper helper()
	//    {
	//        return new Helper(this);
	//    }
	//
	//    @AllArgsConstructor(access = AccessLevel.PRIVATE)
	//    public static final class Helper
	//    {
	//        private final Meta _meta;
	//
	//        private static Function<Object, OptionalLong> TO_LONG = o -> {
	//
	//            if(o instanceof String)
	//            {
	//                return OptionalLong.of(Long.valueOf((String) o));
	//            }
	//            else if(o instanceof Number)
	//            {
	//                return OptionalLong.of(((Number) o).longValue());
	//            }
	//
	//            return OptionalLong.empty();
	//
	//        };
	//
	//        public OptionalLong getDeployment()
	//        {
	//            return _meta.get(DEPLOYMENT).map(TO_LONG).orElse(OptionalLong
	//                    .empty());
	//        }
	//
	//        public OptionalLong getAccount()
	//        {
	//            return _meta.get(ACCOUNT).map(TO_LONG).orElse(OptionalLong
	//                    .empty());
	//        }
	//
	//        public OptionalLong getApplication()
	//        {
	//            return _meta.get(APPLICATION).map(TO_LONG).orElse(OptionalLong
	//                    .empty());
	//        }
	//    }
	//

}
