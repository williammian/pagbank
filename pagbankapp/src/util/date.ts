export function toLocalISOString(date: Date): string {
  const timezoneOffset = date.getTimezoneOffset();
  const offsetHours = Math.floor(Math.abs(timezoneOffset) / 60);
  const offsetMinutes = Math.abs(timezoneOffset) % 60;
  const offsetSign = timezoneOffset <= 0 ? '+' : '-';
  
  const localISOTime = new Date(date.getTime() - (timezoneOffset * 60000)).toISOString().slice(0, -1);
  
  return `${localISOTime}${offsetSign}${String(offsetHours).padStart(2, '0')}:${String(offsetMinutes).padStart(2, '0')}`;
}