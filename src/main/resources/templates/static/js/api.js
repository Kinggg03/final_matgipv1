// Axios 요청 헤더에 CSRF 토큰 자동 추가 (AUTH-06)
axios.interceptors.request.use(config => {
    const token = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
    if (token && header) {
        config.headers[header] = token;
    }
    return config;
});

// 비회원 Axios 요청 시 401 처리 및 로그인 페이지 이동 (AUTH-05)
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            alert("로그인이 필요한 서비스입니다.");
            const currentPath = window.location.pathname;
            window.location.href = `/member/login?redirect=${encodeURIComponent(currentPath)}`;
        }
        return Promise.reject(error);
    }
);