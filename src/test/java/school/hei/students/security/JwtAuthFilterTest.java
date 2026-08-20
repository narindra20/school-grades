package school.hei.students.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {
  @Mock private JwtUtil jwtUtil;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;
  private JwtAuthFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthFilter(jwtUtil);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void authenticates_from_authorization_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer header-token");
    when(jwtUtil.isValid("header-token")).thenReturn(true);
    when(jwtUtil.extractEmail("header-token")).thenReturn("admin@hei.school");
    when(jwtUtil.extractRole("header-token")).thenReturn("ADMIN");

    filter.doFilterInternal(request, response, filterChain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getName()).isEqualTo("admin@hei.school");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void falls_back_to_query_param_when_header_absent() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("token")).thenReturn("query-token");
    when(jwtUtil.isValid("query-token")).thenReturn(true);
    when(jwtUtil.extractEmail("query-token")).thenReturn("student@hei.school");
    when(jwtUtil.extractRole("query-token")).thenReturn("STUDENT");

    filter.doFilterInternal(request, response, filterChain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getName()).isEqualTo("student@hei.school");
  }

  @Test
  void header_takes_priority_over_query_param() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Bearer header-token");
    when(jwtUtil.isValid("header-token")).thenReturn(true);
    when(jwtUtil.extractEmail("header-token")).thenReturn("header@hei.school");
    when(jwtUtil.extractRole("header-token")).thenReturn("ADMIN");

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
        .isEqualTo("header@hei.school");
  }

  @Test
  void no_authentication_when_token_invalid() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("token")).thenReturn("bad-token");
    when(jwtUtil.isValid("bad-token")).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void no_authentication_when_neither_header_nor_param_present() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);
    when(request.getParameter("token")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
