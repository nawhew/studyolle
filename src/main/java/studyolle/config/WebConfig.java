package studyolle.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import studyolle.notification.application.NotificationInterceptor;
import studyolle.notification.domain.NotificationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
//@EnableWebMvc // 주의! 해당 어노테이션 사용 시 spring mvc의 설정을 안쓰게 된다!
public class WebConfig implements WebMvcConfigurer {

    private final NotificationInterceptor notificationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> staticPatterns = Arrays.stream(StaticResourceLocation.values())
                                            .flatMap(StaticResourceLocation::getPatterns)
                                            .collect(Collectors.toList());
        staticPatterns.add("/node_modules/**");
        registry.addInterceptor(this.notificationInterceptor)
                .excludePathPatterns(staticPatterns);
    }
}
