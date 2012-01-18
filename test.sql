declare
p_success   varchar(255);
p_reply     varchar(255);
begin
  gokemal.p_send_email (p_to_addr => 'rajesh.kumar@sungardhe.com',
                p_from_addr => 'rajesh.kumar@sungardhe.com',
                p_subject => 'Password Reset Request',
                p_message => 'Dear Vijendra <br> You are receiving this email. Use this link',
                p_success_out => p_success,
                p_reply_out => p_reply);
  dbms_output.put_line(p_success);
dbms_output.put_line(p_reply);  
                
end;
