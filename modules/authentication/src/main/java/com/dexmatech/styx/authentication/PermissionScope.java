package com.dexmatech.styx.authentication;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gszeliga on 22/12/14.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@Slf4j
public class PermissionScope {

//    private static final Map<String, PermissionScope> memoization = new HashMap<>();
//
//    private final Functionality functionality;
//    private final Action action;
//
//    private static final Pattern SCOPE = Pattern.compile("(\\w+)_(\\w+)");
//
//    @Override
//    public String toString() {
//        return asString(functionality, action);
//    }
//
//    public static String asString(Functionality functionality, Action action){
//
//        return functionality.name()+"_"+action.name();
//    }
//
//    public static PermissionScope of(Functionality functionality, Action action)
//    {
//        //TODO intern?
//        String key = asString(functionality, action);
//
//        if(memoization.containsKey(key))
//        {
//            return memoization.get(key);
//        }
//        else
//        {
//            PermissionScope scope = new PermissionScope(functionality, action);
//
//            memoization.put(key, scope);
//
//            return scope;
//        }
//    }
//
//    public static Optional<PermissionScope> from(String scope)
//    {
//        final Matcher matcher = SCOPE.matcher(scope);
//
//        if(matcher.matches())
//        {
//            String functionality = matcher.group(1).trim().toUpperCase();
//            String action = matcher.group(2).trim().toUpperCase();
//
//            try
//            {
//                return Optional.ofNullable(of(Functionality.valueOf(functionality), Action.valueOf(action)));
//            }
//            catch(Exception e)
//            {
//                log.warn("Bad scope '{}' detected. Impossible to transform into a valid scope", scope);
//            }
//
//        }
//
//        return Optional.empty();
//    }
}
