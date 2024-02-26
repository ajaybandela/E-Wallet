import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RedisTemplate<String,User> redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    String addUser(UserRequest userRequest){

        User user = User.builder().userName(userRequest.getUserName()).age(userRequest.getAge()).mobNum(userRequest.getMobNo()).build();

        // save it to db
        userRepository.save(user);

        // save it to cache
        saveInCache(user);

        return "User Added SuccessFully";
    }
    public void saveInCache(User user){

        Map map = objectMapper.convertValue(user, Map.class);

        redisTemplate.opsForHash().putAll(user.getUserName(),map);

        redisTemplate.expire(user.getUserName(), Duration.ofHours(12));
    }
    public User findUserByUserName(String userName){
        // logic
        // 1. FInd in redis cache
        Map map = redisTemplate.opsForHash().entries(userName);

        User user = null;
        // if not found in the redis/map
        if(map == null){
            // Find the userObject from userRepo
            user = userRepository.findByUserName(userName);
            // save that found user in the cache
            saveInCache(user);
        }else{
            // we found out the user Object
            user = objectMapper.convertValue(map,User.class);
            return user;
        }
        return user;
    }
}
