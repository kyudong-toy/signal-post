import dayjs from 'dayjs';
import 'dayjs/locale/ko'; // 한국어 로케일 import
import relativeTime from 'dayjs/plugin/relativeTime';

// 1. 필요한 플러그인과 로케일을 설정합니다. (최초 한 번만 하면 됩니다)
dayjs.extend(relativeTime);
dayjs.locale('ko');

/**
 * 날짜 문자열을 받아서 상대 시간으로 변환하는 함수
 * @param date - '2025-09-01T15:00:00' 같은 ISO 형식의 문자열
 * @returns '방금 전', '5분 전', '1시간 전' 등의 문자열
 */
export const formatRelativeTime = (date: string) => {
  return dayjs(date).fromNow();
};

/**
 * 날짜 문자열을 받아서 'YYYY.MM.DD' 형식으로 변환하는 함수
 * @param date - '2025-09-01T15:00:00' 같은 ISO 형식의 문자열
 * @param format - 'YYYY.MM.DD' 같은 포맷형태
 * @returns '2025.09.01' 같은 문자열
 */
export const dataUtils = (date: string, format = 'YYYY.MM.DD') => {
  return dayjs(date).format(format);
};